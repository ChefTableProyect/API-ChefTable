package com.proyecto.spring_proyecto.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.stereotype.Repository;

import com.proyecto.spring_proyecto.core.entity.Producto;

@Repository
public interface IProductoDAO extends JpaRepository<Producto, Long> {
     public List<Producto> findAllByOrderByNombreAsc();
     List<Producto> findAllByActiveTrueOrderByNombreAsc();

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("select p from Producto p where p.id = :id")
     Optional<Producto> findByIdForUpdate(@Param("id") Long id);

     @Lock(LockModeType.PESSIMISTIC_WRITE)
     @Query("select p from Producto p where p.id in :ids order by p.id")
     List<Producto> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
