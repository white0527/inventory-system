package com.example.demo.controller;

import com.example.demo.dto.SalesRequest;
import com.example.demo.entity.SalesOrder;
import com.example.demo.entity.SalesOrderItem;
import com.example.demo.repository.SalesOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "*")
public class SalesController {

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @PostMapping
    public Map<String, Object> createOrder(@RequestBody SalesRequest request) {
        // 1. 建立主單
        SalesOrder order = new SalesOrder();
        // 自動生成單號 (範例：S + 年月日 + 時間戳) -> S20260204123456
        String orderNo = "S" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        
        order.setOrderNumber(orderNo);
        order.setOrderDate(LocalDate.parse(request.date));
        order.setBillingMonth(request.billingMonth);
        order.setCustomerCode(request.customerCode);
        order.setCustomerName(request.customerName);
        order.setCustomerAddress(request.customerAddress);
        order.setCreator(request.creator);
        
        // 2. 處理明細
        double total = 0;
        ArrayList<SalesOrderItem> itemList = new ArrayList<>();
        
        for (SalesRequest.Item reqItem : request.items) {
            SalesOrderItem item = new SalesOrderItem();
            item.setProductCode(reqItem.productCode);
            item.setProductName(reqItem.productName);
            item.setCarType(reqItem.carType);
            item.setQuantity(reqItem.quantity);
            item.setPrice(reqItem.price);
            item.setAmount(reqItem.amount);
            item.setNote(reqItem.note);
            item.setLocation(reqItem.location);
            item.setSalesOrder(order); // 綁定主單
            
            itemList.add(item);
            total += reqItem.amount;
        }
        
        order.setItems(itemList);
        order.setTotalAmount(total);

        // 3. 存入 Supabase
        salesOrderRepository.save(order);

        // 4. 回傳成功訊息
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success");
        response.put("orderNumber", orderNo);
        return response;
    }
}