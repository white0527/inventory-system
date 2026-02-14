package com.example.demo.entity;

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
    private Long id; // 自動編號主鍵，解決代號重複導致的匯入失敗問題

    @Column(name = "商品代號")
    private String code; // 對應 Excel A 欄

    @Column(name = "車種")
    private String carModel; // 對應 Excel B 欄

    @Column(name = "名稱")
    private String name; // 對應 Excel C 欄

    @Column(name = "單位")
    private String unit; // 對應 Excel D 欄

    @Column(name = "原廠代號")
    private String originalCode; // 對應 Excel E 欄

    @Column(name = "庫存量")
    private Integer stock; // 對應 Excel F 欄

    @Column(name = "車行價")
    private Integer pricePeer; // 對應 Excel G 欄

    @Column(name = "零售價")
    private Integer priceRetail; // 對應 Excel H 欄

    @Column(name = "成本價")
    private Integer priceCost; // 補回此欄位，解決 priceCost cannot be resolved 報錯

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
}