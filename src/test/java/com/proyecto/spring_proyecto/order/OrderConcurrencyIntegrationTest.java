package com.proyecto.spring_proyecto.order;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.proyecto.spring_proyecto.core.entity.OrderStatus;
import com.proyecto.spring_proyecto.core.entity.Producto;
import com.proyecto.spring_proyecto.core.entity.Categoria;
import com.proyecto.spring_proyecto.order.dto.CreateOrderItemRequest;
import com.proyecto.spring_proyecto.order.dto.CreateOrderRequest;
import com.proyecto.spring_proyecto.repository.ICustomerOrderDAO;
import com.proyecto.spring_proyecto.repository.ICategoriaDAO;
import com.proyecto.spring_proyecto.repository.IProductoDAO;
import com.proyecto.spring_proyecto.shared.ApiException;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class OrderConcurrencyIntegrationTest {

    @Container
    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("cheftable_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
        // Clave Base64 exclusiva para pruebas; evita depender del JWT_SECRET local o de CI.
        registry.add("security.jwt.secret",
                () -> "dGVzdC1vbmx5LWp3dC1zZWNyZXQtMzItYnl0ZXMtbWluaW11bQ==");
    }

    @Autowired
    private OrderApplicationService orderService;

    @Autowired
    private ICustomerOrderDAO orderRepository;

    @Autowired
    private IProductoDAO productRepository;

    @Autowired
    private ICategoriaDAO categoryRepository;

    @BeforeEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    @Test
    void categoryAuditFieldsAreGenerated() {
        var category = new Categoria();
        category.setNombre("Entradas");
        category.setDescripcion("Platos para compartir");

        var saved = categoryRepository.saveAndFlush(category);

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedBy()).isEqualTo("system");
        assertThat(saved.getUpdatedBy()).isEqualTo("system");
    }

    @Test
    void onlyOneOrderCanTakeTheLastUnit() throws Exception {
        var product = saveProduct("Último lomo", 1);
        var request = request(List.of(new CreateOrderItemRequest(product.getId(), 1, null)));

        var results = runConcurrently(20, index -> orderService.create("stock-race-" + index, request));

        assertThat(results.stream().filter(result -> result instanceof OrderCreationResult).count()).isEqualTo(1);
        assertThat(results.stream().filter(result -> result instanceof ApiException exception
                && exception.getType().equals("INSUFFICIENT_STOCK")).count()).isEqualTo(19);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isZero();
        assertThat(orderRepository.count()).isEqualTo(1);
    }

    @Test
    void retryingTheSameRequestDoesNotDuplicateTheOrderOrStockDiscount() {
        var product = saveProduct("Arroz norteño", 3);
        var request = request(List.of(new CreateOrderItemRequest(product.getId(), 2, null)));

        var first = orderService.create("same-request-001", request);
        var retry = orderService.create("same-request-001", request);

        assertThat(first.replayed()).isFalse();
        assertThat(retry.replayed()).isTrue();
        assertThat(retry.order().id()).isEqualTo(first.order().id());
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(1);
    }

    @Test
    void simultaneousRetriesWithTheSameKeyReturnOneOrder() throws Exception {
        var product = saveProduct("Tacu tacu", 10);
        var request = request(List.of(new CreateOrderItemRequest(product.getId(), 1, null)));

        var results = runConcurrently(10, index -> orderService.create("simultaneous-retry-001", request));

        assertThat(results).allMatch(OrderCreationResult.class::isInstance);
        var orders = results.stream().map(OrderCreationResult.class::cast).toList();
        assertThat(orders).extracting(result -> result.order().id()).containsOnly(orders.get(0).order().id());
        assertThat(orders.stream().filter(result -> !result.replayed()).count()).isEqualTo(1);
        assertThat(orderRepository.count()).isEqualTo(1);
        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(9);
    }

    @Test
    void reusingAnIdempotencyKeyWithDifferentDataIsRejected() {
        var product = saveProduct("Ceviche", 5);
        orderService.create("reused-key-001", request(List.of(
                new CreateOrderItemRequest(product.getId(), 1, null))));

        assertThatThrownBy(() -> orderService.create("reused-key-001", request(List.of(
                new CreateOrderItemRequest(product.getId(), 2, null)))))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getType()).isEqualTo("IDEMPOTENCY_KEY_REUSED"));
        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(4);
    }

    @Test
    void cancellingReturnsStockExactlyOnce() {
        var product = saveProduct("Ají de gallina", 2);
        var created = orderService.create("cancel-order-001", request(List.of(
                new CreateOrderItemRequest(product.getId(), 1, null))));

        orderService.updateStatus(created.order().id(), OrderStatus.CANCELLED);

        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(2);
        assertThatThrownBy(() -> orderService.updateStatus(created.order().id(), OrderStatus.CANCELLED))
                .isInstanceOfSatisfying(ApiException.class,
                        exception -> assertThat(exception.getType()).isEqualTo("INVALID_ORDER_TRANSITION"));
        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(2);
    }

    @Test
    void cancellingFoodAlreadyInPreparationDoesNotReturnConsumedStock() {
        var product = saveProduct("Seco de res", 2);
        var created = orderService.create("preparing-cancel-001", request(List.of(
                new CreateOrderItemRequest(product.getId(), 1, null))));

        orderService.updateStatus(created.order().id(), OrderStatus.PREPARING);
        orderService.updateStatus(created.order().id(), OrderStatus.CANCELLED);

        assertThat(productRepository.findById(product.getId()).orElseThrow().getCantidad()).isEqualTo(1);
    }

    @Test
    void oppositeItemOrderDoesNotCreateCrossLocks() throws Exception {
        int attempts = 12;
        var firstProduct = saveProduct("Entrada", attempts);
        var secondProduct = saveProduct("Fondo", attempts);

        var results = runConcurrently(attempts, index -> {
            var items = index % 2 == 0
                    ? List.of(
                            new CreateOrderItemRequest(firstProduct.getId(), 1, null),
                            new CreateOrderItemRequest(secondProduct.getId(), 1, null))
                    : List.of(
                            new CreateOrderItemRequest(secondProduct.getId(), 1, null),
                            new CreateOrderItemRequest(firstProduct.getId(), 1, null));
            return orderService.create("multi-item-" + index, request(items));
        });

        assertThat(results).allMatch(OrderCreationResult.class::isInstance);
        assertThat(productRepository.findById(firstProduct.getId()).orElseThrow().getCantidad()).isZero();
        assertThat(productRepository.findById(secondProduct.getId()).orElseThrow().getCantidad()).isZero();
    }

    private Producto saveProduct(String name, int stock) {
        var product = new Producto();
        product.setNombre(name);
        product.setCantidad(stock);
        product.setPrecio(new BigDecimal("25.00"));
        product.setActive(true);
        return productRepository.saveAndFlush(product);
    }

    private CreateOrderRequest request(List<CreateOrderItemRequest> items) {
        return new CreateOrderRequest("MESA-01", items);
    }

    private List<Object> runConcurrently(int tasks, ThrowingTask task) throws Exception {
        var executor = Executors.newFixedThreadPool(tasks);
        var ready = new CountDownLatch(tasks);
        var start = new CountDownLatch(1);
        try {
            var futures = new ArrayList<java.util.concurrent.Future<Object>>();
            for (int index = 0; index < tasks; index++) {
                int taskIndex = index;
                futures.add(executor.submit(() -> {
                    ready.countDown();
                    start.await();
                    try {
                        return task.run(taskIndex);
                    } catch (RuntimeException exception) {
                        return exception;
                    }
                }));
            }
            ready.await();
            start.countDown();
            var results = new ArrayList<Object>();
            for (var future : futures) {
                results.add(future.get());
            }
            return results;
        } finally {
            executor.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface ThrowingTask {
        Object run(int index);
    }
}
