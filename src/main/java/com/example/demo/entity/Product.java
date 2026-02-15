package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String code;        // 零件代號
    private String name;        // 零件名稱
    private String carModel;    // 適用車種
    private Double pricePeer;   // 車行價
    private Double priceRetail; // 零售價
    private Integer stock;      // 庫存

    @Column(name = "updated_at")
    private LocalDateTime updatedAt; // 統一使用 LocalDateTime

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now(); // 每次存檔自動更新時間
    }

    // --- Getter and Setter ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public Double getPricePeer() { return pricePeer; }
    public void setPricePeer(Double pricePeer) { this.pricePeer = pricePeer; }
    public Double getPriceRetail() { return priceRetail; }
    public void setPriceRetail(Double priceRetail) { this.priceRetail = priceRetail; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}