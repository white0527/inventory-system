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
     * 這樣手機第二次登入時，只需要下載這段時間內變動的資料即可
     * * @param lastSyncTime 上次手機同步的時間
     * @return 變動過的商品清單
     */
    List<Product> findByUpdatedAtAfter(OffsetDateTime lastSyncTime);
    
    /**
     * 您也可以保留原本根據商品代號搜尋的功能，供後端管理介面使用
     */
    List<Product> findBy商品代號Containing(String 商品代號);
}