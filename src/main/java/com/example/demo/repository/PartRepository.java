package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Part;

public interface PartRepository extends JpaRepository<Part, Long> {
    // 透過 SKU 尋找商品 (自動產生 SQL: select * from parts where sku = ?)
    Optional<Part> findBySku(String sku);
}