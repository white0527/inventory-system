package com.example.demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "products")
public class Product {
    @Id
    private String code;       // 商品代號
    private String name;       // 商品名稱
    private String carModel;   // 車種
    private Integer stock;     // 庫存
    private String location;   // 儲位
    private Integer priceRetail; // 散客價
    private Integer pricePeer;   // 同業價
    private Integer priceCost;   // 成本

    // Getters & Setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public Integer getPriceRetail() { return priceRetail; }
    public void setPriceRetail(Integer priceRetail) { this.priceRetail = priceRetail; }
    public Integer getPricePeer() { return pricePeer; }
    public void setPricePeer(Integer pricePeer) { this.pricePeer = pricePeer; }
    public Integer getPriceCost() { return priceCost; }
    public void setPriceCost(Integer priceCost) { this.priceCost = priceCost; }
}