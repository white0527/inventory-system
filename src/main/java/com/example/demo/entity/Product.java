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

    // 將 Java 變數與資料庫中文欄位連結
    @Column(name = "商品代號", unique = true) 
    private String code;

    @Column(name = "名稱")
    private String name;

    @Column(name = "車種")
    private String carModel;

    @Column(name = "車行價")
    private Double pricePeer;

    @Column(name = "零售價")
    private Double priceRetail;

    @Column(name = "庫存")
    private Integer stock;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 必須定義此欄位，下方的 Getter/Setter 才能運作
    @Column(name = "is_deleted")
    private boolean isDeleted = false;

    // 自動更新時間標記
    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Getter & Setter ---
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
    
    public boolean isIsDeleted() { return isDeleted; }
    public void setIsDeleted(boolean isDeleted) { this.isDeleted = isDeleted; }
}