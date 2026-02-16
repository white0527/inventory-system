package com.example.demo.controller;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping("/download")
    public List<Product> downloadUpdates(@RequestParam String lastSyncTime) {
        try {
            // 解析前端 ISO 時間，對齊 LocalDateTime 類型
            LocalDateTime lastUpdate = ZonedDateTime.parse(lastSyncTime).toLocalDateTime();
            
            // 呼叫 Repository 進行增量查詢
            return productRepository.findByUpdatedAtAfter(lastUpdate);
        } catch (Exception e) {
            // 解析失敗（初次登入）則回傳全量
            return productRepository.findAll();
        }
    }

    @GetMapping("/full-sync")
    public List<Product> getFullData() {
        return productRepository.findAll();
    }
}