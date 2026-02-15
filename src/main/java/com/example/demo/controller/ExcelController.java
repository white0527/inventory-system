package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/excel")
public class ExcelController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/import")
    public String importExcel(@RequestBody List<Product> products) {
        // 因為 id 是自動生成的 Long，不再需要用 String 代號去 findById
        // 直接批次儲存即可，這能解決 image_c63466.jpg 第 104 行的錯誤
        productRepository.saveAll(products);
        return "成功匯入 " + products.size() + " 筆資料";
    }
}