package com.example.demo.dto;

import java.util.List;

public class SalesRequest {
    public String customerCode;
    public String customerName;
    public String customerAddress;
    public String date;
    public String billingMonth;
    public String creator;
    public List<Item> items;

    public static class Item {
        public String productCode;
        public String productName;
        public String carType;
        public Integer quantity;
        public Double price;
        public Double amount;
        public String note;
        public String location;
    }
}