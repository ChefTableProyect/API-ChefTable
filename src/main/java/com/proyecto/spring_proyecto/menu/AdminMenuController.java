package com.proyecto.spring_proyecto.menu;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.menu.dto.AdminMenuItemRequest;
import com.proyecto.spring_proyecto.menu.dto.MenuItemResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/menu/items")
public class AdminMenuController {

    private final AdminMenuService menuService;

    public AdminMenuController(AdminMenuService menuService) {
        this.menuService = menuService;
    }

    @GetMapping
    public List<MenuItemResponse> findAll() {
        return menuService.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MenuItemResponse create(@Valid @RequestBody AdminMenuItemRequest request) {
        return menuService.create(request);
    }

    @PutMapping("/{id}")
    public MenuItemResponse update(@PathVariable Long id, @Valid @RequestBody AdminMenuItemRequest request) {
        return menuService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable Long id) {
        menuService.deactivate(id);
    }
}
