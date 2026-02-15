package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*") // 確保手機端與 Render 網域連通
public class ProductController {

    @Autowired
    private ProductRepository productRepository;

    /**
     * 取得所有商品，並支援關鍵字搜尋
     * 支援輸入「商品代號」或「名稱」進行過濾
     */
    @GetMapping
    public List<Product> getProducts(@RequestParam(required = false) String search) {
        if (search != null && !search.isEmpty()) {
            // 同時搜尋代號與名稱，確保查價效率
            List<Product> results = productRepository.findByCodeContaining(search);
            if (results.isEmpty()) {
                results = productRepository.findByNameContaining(search);
            }
            return results;
        }
        return productRepository.findAll();
    }

    /**
     * 根據 ID 查詢單筆商品
     * 修正：參數類型改為 Long 以對應資料庫 int8 型態
     */
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable Long id) {
        return productRepository.findById(id).orElse(null);
    }

    /**
     * 刪除商品
     * 修正：參數類型改為 Long
     */
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}