package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    // === 1. 下載所有資料 ===
    @GetMapping("/all")
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // === 2. 查價功能 ===
    @GetMapping("/search")
    public Product searchProduct(@RequestParam String keyword) {
        String key = keyword.toUpperCase();
        return productRepository.findById(key)
                .orElseGet(() -> {
                    List<Product> list = productRepository.findByNameContaining(key);
                    if (!list.isEmpty()) {
                        return list.get(0);
                    }
                    return null;
                });
    }

    // === 3. 修改商品 (已修正報錯並新增欄位) ===
    @PostMapping("/update")
    public Map<String, Object> updateProduct(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        Optional<Product> opt = productRepository.findById(code);
        
        if (opt.isPresent()) {
            Product p = opt.get();
            
            // 更新價格與庫存
            if (payload.containsKey("priceRetail")) p.setPriceRetail(Integer.parseInt(payload.get("priceRetail").toString()));
            if (payload.containsKey("pricePeer")) p.setPricePeer(Integer.parseInt(payload.get("pricePeer").toString()));
            if (payload.containsKey("priceCost")) p.setPriceCost(Integer.parseInt(payload.get("priceCost").toString()));
            if (payload.containsKey("stock")) p.setStock(Integer.parseInt(payload.get("stock").toString()));
            
            // 新增：更新單位與原廠代號
            if (payload.containsKey("unit")) p.setUnit((String) payload.get("unit"));
            if (payload.containsKey("originalCode")) p.setOriginalCode((String) payload.get("originalCode"));
            
            // ⚠️ 舊的 setLocation 已刪除，解決紅字報錯
            
            productRepository.save(p);
            return Map.of("success", true, "message", "更新成功");
        }
        return Map.of("success", false, "message", "找不到商品");
    }
}