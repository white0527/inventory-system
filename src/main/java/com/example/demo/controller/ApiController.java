package com.example.demo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Part;
import com.example.demo.repository.PartRepository;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*") // 允許前端跨網域呼叫
public class ApiController {

    @Autowired
    private PartRepository partRepository;

    // 前端輸入代號後，呼叫此 API 查詢商品資訊
    @GetMapping("/products/{code}")
    public ResponseEntity<?> getProduct(@PathVariable String code) {
        Optional<Part> partOpt = partRepository.findBySku(code);

        if (partOpt.isPresent()) {
            Part part = partOpt.get();
            
            // 整理要回傳給前端的 JSON 格式
            Map<String, Object> response = new HashMap<>();
            response.put("code", part.getSku());
            response.put("name", part.getName());
            response.put("price", part.getPrice());
            response.put("stock", part.getQuantity()); // 前端顯示庫存用
            response.put("location", part.getLocation());
            response.put("carType", part.getCarType());

            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}