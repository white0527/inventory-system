package com.example.demo.repository;

import com.example.demo.entity.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
}