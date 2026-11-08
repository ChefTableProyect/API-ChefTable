package com.proyecto.spring_proyecto.menu;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.spring_proyecto.core.entity.Producto;
import com.proyecto.spring_proyecto.menu.dto.AdminMenuItemRequest;
import com.proyecto.spring_proyecto.menu.dto.MenuItemResponse;
import com.proyecto.spring_proyecto.repository.ICategoriaDAO;
import com.proyecto.spring_proyecto.repository.IProductoDAO;
import com.proyecto.spring_proyecto.shared.ApiException;

@Service
public class AdminMenuService {

    private final IProductoDAO productRepository;
    private final ICategoriaDAO categoryRepository;

    public AdminMenuService(IProductoDAO productRepository, ICategoriaDAO categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuItemResponse> findAll() {
        return productRepository.findAllByOrderByNombreAsc().stream().map(this::toResponse).toList();
    }

    @Transactional
    public MenuItemResponse create(AdminMenuItemRequest request) {
        var product = new Producto();
        apply(product, request);
        return toResponse(productRepository.save(product));
    }

    @Transactional
    public MenuItemResponse update(Long id, AdminMenuItemRequest request) {
        var product = findProduct(id);
        apply(product, request);
        return toResponse(product);
    }

    @Transactional
    public void deactivate(Long id) {
        var product = findProduct(id);
        product.setActive(false);
    }

    private void apply(Producto product, AdminMenuItemRequest request) {
        var category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "La categoría no existe"));
        product.setNombre(request.name().trim());
        product.setPrecio(request.price());
        product.setCantidad(request.stock());
        product.setCategoria(category);
        product.setActive(request.active());
    }

    private Producto findProduct(Long id) {
        return productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", "El plato no existe"));
    }

    private MenuItemResponse toResponse(Producto product) {
        return new MenuItemResponse(
                product.getId(),
                product.getNombre(),
                product.getPrecio(),
                product.getCantidad(),
                product.isActive(),
                product.isActive() && product.getCantidad() > 0,
                product.getCategoria() == null ? null : product.getCategoria().getId(),
                product.getCategoria() == null ? "Sin categoría" : product.getCategoria().getNombre());
    }
}
