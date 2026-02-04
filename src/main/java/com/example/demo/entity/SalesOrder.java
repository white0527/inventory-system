package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String orderNumber;
    private LocalDate orderDate;
    private String billingMonth;
    private String customerCode;
    private String customerName;
    private String customerAddress;
    private Double totalAmount;
    private String creator;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    // 關聯明細 (一對多)
    @OneToMany(mappedBy = "salesOrder", cascade = CascadeType.ALL)
    private List<SalesOrderItem> items;

    // Getters and Setters (省略不寫，Lombok 會處理，若沒用 Lombok 請手動產生)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOrderNumber() { return orderNumber; }
    public void setOrderNumber(String orderNumber) { this.orderNumber = orderNumber; }
    public LocalDate getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDate orderDate) { this.orderDate = orderDate; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String billingMonth) { this.billingMonth = billingMonth; }
    public String getCustomerCode() { return customerCode; }
    public void setCustomerCode(String customerCode) { this.customerCode = customerCode; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }
    public String getCreator() { return creator; }
    public void setCreator(String creator) { this.creator = creator; }
    public List<SalesOrderItem> getItems() { return items; }
    public void setItems(List<SalesOrderItem> items) { this.items = items; }
}