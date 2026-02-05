package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "parts") // 對應 Supabase 資料庫的 parts 表
public class Part {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku; // 商品代號 (對應前端的 Code)

    @Column(nullable = false)
    private String name; // 商品名稱

    private String carType; // 車種 (例如: GY6, VJR)

    private Double price; // 單價

    private Integer quantity; // 庫存數量

    private String location; // 倉庫儲位 (例如: A-01)

    // === Getters and Setters ===
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }
    
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}