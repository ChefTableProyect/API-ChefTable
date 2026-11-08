package com.proyecto.spring_proyecto.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.application.interfaces.IProductoService;
import com.proyecto.spring_proyecto.core.entity.Producto;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/productos")
public class ProductoController {

    private final IProductoService productoService;
 
    @GetMapping
    public List<Producto> getAllProductos() {
        return productoService.cargarProductos();
    }

    // Método GET para obtener un producto por ID
    @GetMapping("/{id}")
    public ResponseEntity<Producto> getProductoById(@PathVariable Long id) {
        Optional<Producto> producto = productoService.getProductoById(id);
        return producto.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Método POST para crear un nuevo producto
    @PostMapping
    public String createProducto(@RequestBody Producto producto) {
        return productoService.guardarProducto(producto);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProducto(@PathVariable Long id) {
        // if (!productoService.existsById(id)) {
        // return ResponseEntity.notFound().build();
        // }
        productoService.eliminarProducto(id);
        return ResponseEntity.noContent().build();
    }

    // Método PUT para actualizar un producto existente TODO
    // @PutMapping("/{id}")
    // public ResponseEntity<Producto> updateProducto(@PathVariable Long id,
    // @RequestBody Producto productoDetails) {
    // Optional<Producto> productoOptional = productoService.findById(id);

    // if (!productoOptional.isPresent()) {
    // return ResponseEntity.notFound().build();
    // }

    // Producto producto = productoOptional.get();
    // producto.setNombre(productoDetails.getNombre());
    // producto.setPrecio(productoDetails.getPrecio());

    // Producto updatedProducto = productoService.save(producto);
    // return ResponseEntity.ok(updatedProducto);
    // }

    // Método DELETE para eliminar un producto


}
