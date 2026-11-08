package com.proyecto.spring_proyecto.table;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.proyecto.spring_proyecto.repository.IDiningTableDAO;
import com.proyecto.spring_proyecto.table.dto.DiningTableResponse;

@RestController
@RequestMapping("/api/v1/tables")
public class DiningTableController {

    private final IDiningTableDAO tableRepository;

    public DiningTableController(IDiningTableDAO tableRepository) {
        this.tableRepository = tableRepository;
    }

    @GetMapping("/{code}")
    public ResponseEntity<DiningTableResponse> findByCode(@PathVariable String code) {
        return tableRepository.findByCodeIgnoreCaseAndActiveTrue(code)
                .map(table -> ResponseEntity.ok(new DiningTableResponse(
                        table.getId(), table.getCode(), table.getName(), table.isActive())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
