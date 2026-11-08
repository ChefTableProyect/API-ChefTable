package com.proyecto.spring_proyecto.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import com.proyecto.spring_proyecto.core.entity.CustomerOrder;
import com.proyecto.spring_proyecto.core.entity.OrderStatus;

public interface ICustomerOrderDAO extends JpaRepository<CustomerOrder, Long> {

    @EntityGraph(attributePaths = {"diningTable", "items", "items.product"})
    Optional<CustomerOrder> findByPublicCodeIgnoreCase(String publicCode);

    @EntityGraph(attributePaths = {"diningTable", "items", "items.product"})
    Optional<CustomerOrder> findByIdempotencyKey(String idempotencyKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from CustomerOrder o where o.id = :id")
    Optional<CustomerOrder> findByIdForUpdate(@Param("id") Long id);

    @EntityGraph(attributePaths = {"diningTable", "items", "items.product"})
    List<CustomerOrder> findAllByStatusInOrderByCreatedAtAsc(Collection<OrderStatus> statuses);
}
