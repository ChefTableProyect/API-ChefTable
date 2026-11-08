package com.proyecto.spring_proyecto.menu;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.proyecto.spring_proyecto.menu.dto.CategoryResponse;
import com.proyecto.spring_proyecto.menu.dto.MenuItemResponse;
import com.proyecto.spring_proyecto.repository.ICategoriaDAO;
import com.proyecto.spring_proyecto.repository.IProductoDAO;

@Service
@Transactional(readOnly = true)
public class MenuQueryService {

    private final IProductoDAO productRepository;
    private final ICategoriaDAO categoryRepository;

    public MenuQueryService(IProductoDAO productRepository, ICategoriaDAO categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<MenuItemResponse> findItems() {
        return productRepository.findAllByActiveTrueOrderByNombreAsc().stream()
                .map(product -> new MenuItemResponse(
                        product.getId(),
                        product.getNombre(),
                        product.getPrecio(),
                        product.getCantidad(),
                        product.isActive(),
                        product.isActive() && product.getCantidad() > 0,
                        product.getCategoria() == null ? null : product.getCategoria().getId(),
                        product.getCategoria() == null ? "Sin categoría" : product.getCategoria().getNombre()))
                .toList();
    }

    public List<CategoryResponse> findCategories() {
        return categoryRepository.findAllByOrderByNombreAsc().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getNombre(),
                        category.getDescripcion()))
                .toList();
    }
}
