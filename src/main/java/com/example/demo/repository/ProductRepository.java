package com.example.demo.repository;

import com.example.demo.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 1. 增量同步核心方法
     * 用於查詢在特定時間點之後有任何變動（新增或修改）的資料。
     * 修正：確保參數類型為 LocalDateTime，以對齊 Product.java 中的 updatedAt 欄位。
     */
    List<Product> findByUpdatedAtAfter(LocalDateTime lastUpdate);

    /**
     * 2. 關鍵字搜尋：代號
     * 支援輸入部分代號進行模糊搜尋。
     */
    List<Product> findByCodeContaining(String code);

    /**
     * 3. 關鍵字搜尋：名稱
     * 支援輸入部分零件名稱進行模糊搜尋。
     */
    List<Product> findByNameContaining(String name);

    /**
     * 4. 關鍵字搜尋：車種
     * 支援根據適用車型進行過濾。
     */
    List<Product> findByCarModelContaining(String carModel);
}