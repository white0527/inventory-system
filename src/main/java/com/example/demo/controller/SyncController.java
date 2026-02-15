package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*")
public class SyncController {

    @Autowired
    private ProductRepository productRepository;

    // 增量同步：只抓更改項
    @GetMapping("/download")
    public List<Product> downloadUpdates(@RequestParam String lastSyncTime) {
        try {
            // 解析前端傳來的 ISO 時間字串並轉為本地時間
            LocalDateTime lastUpdate = ZonedDateTime.parse(lastSyncTime).toLocalDateTime();
            return productRepository.findByUpdatedAtAfter(lastUpdate);
        } catch (Exception e) {
            // 若解析失敗（如第一次登入），回傳全量
            return productRepository.findAll();
        }
    }

    // 全量同步：安裝後第一次搬運
    @GetMapping("/full-sync")
    public List<Product> getFullData() {
        return productRepository.findAll();
    }
}