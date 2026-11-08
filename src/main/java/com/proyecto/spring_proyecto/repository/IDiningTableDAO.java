package com.proyecto.spring_proyecto.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.proyecto.spring_proyecto.core.entity.DiningTable;

public interface IDiningTableDAO extends JpaRepository<DiningTable, Long> {
    Optional<DiningTable> findByCodeIgnoreCaseAndActiveTrue(String code);
}
