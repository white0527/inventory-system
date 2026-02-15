package com.example.demo.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Product;

/**
 * 商品資料存取介面
 * 擴展 JpaRepository 以支援離線同步所需的增量查詢
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * 增量同步核心功能：根據時間戳記撈出「最後更新時間」晚於指定時間的商品
     * 這裡的 UpdatedAt 必須完全對應 Product.java 裡的變數名 updatedAt
     * @param lastSyncTime 上次手機同步的時間
     * @return 變動過的商品清單
     */
    List<Product> findByUpdatedAtAfter(OffsetDateTime lastSyncTime);
    
    /**
     * 修正：將「商品代號」改為對應實體類中的變數名 「code」
     * 這樣 Spring Boot 啟動時才不會因為找不到欄位而崩潰
     */
    List<Product> findByCodeContaining(String code);

    /**
     * 新增：根據名稱搜尋的功能，這在您的查價系統非常實用
     */
    List<Product> findByNameContaining(String name);
}