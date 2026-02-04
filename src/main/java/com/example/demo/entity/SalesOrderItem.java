package com.example.demo.entity;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id")
    @JsonIgnore // 避免 JSON 遞迴
    private SalesOrder salesOrder;

    private String productCode;
    private String productName;
    private String carType;
    private Integer quantity;
    private Double price;
    private Double amount;
    private String note;
    private String location;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public SalesOrder getSalesOrder() { return salesOrder; }
    public void setSalesOrder(SalesOrder salesOrder) { this.salesOrder = salesOrder; }
    public String getProductCode() { return productCode; }
    public void setProductCode(String productCode) { this.productCode = productCode; }
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }
    public String getCarType() { return carType; }
    public void setCarType(String carType) { this.carType = carType; }
    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}