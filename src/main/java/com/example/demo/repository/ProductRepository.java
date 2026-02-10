package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Product;

public interface ProductRepository extends JpaRepository<Product, String> {
    // 模糊搜尋 (找代號或名稱)
    // 這裡會對應到 App 的查價功能
    List<Product> findByNameContaining(String name);
}