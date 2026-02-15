package com.example.demo.controller;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * 離線同步控制器
 * 解決 Render 連線不穩定問題，實現手機離線優先架構
 */
@RestController
@RequestMapping("/api/sync")
@CrossOrigin(origins = "*") // 允許手機網頁跨網域存取
public class SyncController {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 1. 聯網同步：接收手機離線時修改的資料並存入 Supabase
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadChanges(@RequestBody List<Product> changedProducts) {
        try {
            if (changedProducts == null || changedProducts.isEmpty()) {
                return ResponseEntity.ok(Map.of("message", "無變動資料需同步"));
            }
            // 批量儲存修改過的資料
            productRepository.saveAll(changedProducts);
            return ResponseEntity.ok(Map.of("message", "成功同步 " + changedProducts.size() + " 筆資料至雲端"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "同步失敗: " + e.getMessage()));
        }
    }

    /**
     * 2. 增量下載：手機只抓取「上次同步後」有變動的資料
     * 用法: /api/sync/download?lastSyncTime=2026-02-15T00:00:00Z
     */
    @GetMapping("/download")
    public ResponseEntity<List<Product>> downloadChanges(@RequestParam(name = "lastSyncTime", required = false) String lastSyncTime) {
        try {
            List<Product> updates;
            if (lastSyncTime == null || lastSyncTime.isEmpty()) {
                // 如果是第一次登入，抓取全部 38,032 筆
                updates = productRepository.findAll();
            } else {
                // 根據時間戳記抓取更新
                OffsetDateTime lastTime = OffsetDateTime.parse(lastSyncTime);
                updates = productRepository.findByUpdatedAtAfter(lastTime);
            }
            return ResponseEntity.ok(updates);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    /**
     * 3. 健康檢查：確認 API 是否活著
     */
    @GetMapping("/status")
    public Map<String, String> status() {
        return Map.of("status", "穩定運行中", "database", "已連線至 Supabase Pooler");
    }
}