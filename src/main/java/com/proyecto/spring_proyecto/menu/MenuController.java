package com.proyecto.spring_proyecto.menu;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.menu.dto.CategoryResponse;
import com.proyecto.spring_proyecto.menu.dto.MenuItemResponse;

@RestController
@RequestMapping("/api/v1/menu")
public class MenuController {

    private final MenuQueryService menuQueryService;

    public MenuController(MenuQueryService menuQueryService) {
        this.menuQueryService = menuQueryService;
    }

    @GetMapping("/items")
    public List<MenuItemResponse> findItems() {
        return menuQueryService.findItems();
    }

    @GetMapping("/categories")
    public List<CategoryResponse> findCategories() {
        return menuQueryService.findCategories();
    }
}
