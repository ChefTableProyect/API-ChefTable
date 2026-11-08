# Concurrencia, idempotencia y seguridad

## Regla de inventario

Agregar un plato al carrito no crea una reserva. El inventario se asigna cuando el cliente confirma el pedido. Esta regla evita que un carrito abandonado bloquee el último plato.

La confirmación ocurre dentro de una transacción. Todos los productos se bloquean por ID en un orden estable antes de validar o descontar unidades. Así se evitan sobreventas y se reduce el riesgo de deadlocks cuando dos pedidos contienen los mismos platos en distinto orden.

La base de datos agrega una restricción `CHECK (pro_cantidad >= 0)` como última línea de defensa.

## Idempotencia

`POST /api/v1/orders` exige la cabecera `Idempotency-Key`.

- Una clave nueva y un pedido válido producen `201 Created`.
- Repetir la misma clave con el mismo contenido devuelve el pedido original con `200 OK` e `Idempotency-Replayed: true`.
- Reutilizar la clave con otro contenido produce `409 IDEMPOTENCY_KEY_REUSED`.

El servidor almacena un SHA-256 de una representación canónica del pedido. La fila del pedido se inserta antes de descontar inventario para reclamar la clave. La restricción única en la base de datos resuelve incluso dos reintentos que llegan simultáneamente: solo el ganador llega a bloquear y descontar productos.

## Cancelaciones

- Cancelar un pedido `CONFIRMED` devuelve el stock bajo bloqueo transaccional.
- Cancelar un pedido `PREPARING` no devuelve stock, porque la cocina ya pudo consumir los ingredientes.
- El pedido se bloquea al cambiar de estado, por lo que dos operadores no pueden devolver stock dos veces.

## Separación de responsabilidades

- `OrderApplicationService`: coordina casos de uso y transiciones de estado.
- `OrderCreationService`: crea un pedido dentro de una transacción independiente.
- `InventoryService`: aplica exclusivamente las reglas de reserva y devolución de inventario.
- `OrderIdempotencyService`: valida claves y calcula la huella del pedido.
- `OrderResponseMapper`: transforma entidades en respuestas de API.

Esta división aplica responsabilidad única e inversión de dependencias mediante inyección por constructor, sin crear abstracciones que el proyecto todavía no necesita.

## Controles de seguridad

- API sin sesión y autenticación JWT para cocina y administración.
- Roles `KITCHEN` y `ADMIN` aplicados por ruta.
- Endpoints heredados de productos y documentación OpenAPI restringidos a `ADMIN`.
- CORS limitado a orígenes configurados y cabeceras explícitas.
- BCrypt configurable, cabeceras defensivas, límites de tamaño en pedidos y secretos fuera del repositorio.
- Pool de conexiones y cola HTTP acotados para aplicar contrapresión.

En producción todavía conviene colocar la API detrás de un proxy con TLS y rate limiting distribuido. Un limitador en memoria no funcionaría correctamente al ejecutar varias instancias.

## Pruebas

Las pruebas de integración usan MySQL 8.4 mediante Testcontainers y validan:

1. Veinte solicitudes compiten por una unidad: una gana y diecinueve reciben conflicto.
2. Reintentar la misma petición no duplica pedido ni descuento.
3. Diez reintentos simultáneos con la misma clave recuperan un solo pedido.
4. Reutilizar una clave con datos diferentes es rechazado.
5. Cancelar devuelve stock una sola vez.
6. Cancelar durante preparación no devuelve stock consumido.
7. Pedidos con productos en orden opuesto no generan bloqueos cruzados.

Ejecutar con Docker iniciado:

```powershell
.\test-local.ps1
```

El script selecciona el motor Linux de Docker Desktop solo durante esa ejecución. Testcontainers usa además la API 1.44 configurada en `src/test/resources/docker-java.properties`, necesaria para Docker Engine 29 o superior.

## Pruebas de carga con k6

Antes de la carrera por la última unidad, establecer manualmente el stock del producto en `1`:

```powershell
k6 run -e PRODUCT_ID=1 -e VUS=50 -e ITERATIONS=50 load-tests/last-unit-race.js
```

El resultado esperado es exactamente un pedido creado, stock final cero y el resto de solicitudes con `409`.

Para probar reintentos, configurar stock suficiente:

```powershell
k6 run -e PRODUCT_ID=1 -e VUS=10 -e ITERATIONS=20 load-tests/idempotency-retry.js
```

Los umbrales incluidos exigen cero respuestas inesperadas y latencia `p95` menor a 750 ms. Estos valores deben ajustarse según la infraestructura real.
