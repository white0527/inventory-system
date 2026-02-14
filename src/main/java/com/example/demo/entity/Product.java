package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
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
    private Integer stock;

    @Column(name = "車行價")
    private Integer pricePeer;

    @Column(name = "零售價")
    private Integer priceRetail;

    @Column(name = "成本價")
    private Integer priceCost; // 修正關鍵：補回這個變數定義

    // Getters & Setters (請務必包含 getPriceCost 與 setPriceCost)
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
}