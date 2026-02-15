package com.example.demo.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 自動編號主鍵，必須是 Long

    @Column(name = "商品代號")
    private String code;

    @Column(name = "車種")
    private String carModel;

    @Column(name = "名稱")
    private String name;

    @Column(name = "單位")
    private String unit;

    @Column(name = "原廠代號")
    private String originalCode;

    @Column(name = "庫存量")
    private Integer stock; // 類型 int4

    @Column(name = "車行價")
    private Integer pricePeer; // 類型 int4

    @Column(name = "零售價")
    private Integer priceRetail; // 類型 int4

    @Column(name = "成本價")
    private Integer priceCost; // 類型 int4

    // 關鍵修正：確保變數名稱為 updatedAt，首字母大寫的 A 對應 Repository 的 findByUpdatedAt
    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getOriginalCode() { return originalCode; }
    public void setOriginalCode(String originalCode) { this.originalCode = originalCode; }

    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }

    public Integer getPricePeer() { return pricePeer; }
    public void setPricePeer(Integer pricePeer) { this.pricePeer = pricePeer; }

    public Integer getPriceRetail() { return priceRetail; }
    public void setPriceRetail(Integer priceRetail) { this.priceRetail = priceRetail; }

    public Integer getPriceCost() { return priceCost; }
    public void setPriceCost(Integer priceCost) { this.priceCost = priceCost; }

    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}