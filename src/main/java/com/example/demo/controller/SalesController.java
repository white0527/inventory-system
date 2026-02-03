package com.example.demo.controller;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class SalesController {

    // ✅ 正确的 Supabase 網址
    private static final String SUPABASE_URL = "https://txowvwqkruzamxaxzosc.supabase.co";
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    // =========== 1. 查詢訂單列表 ===========
    @GetMapping("/api/sales")
    public Object getAllSalesOrders() {
        System.out.println("🔍 [API模式] 正在查詢所有訂單...");
        try {
            String targetUrl = SUPABASE_URL + "/rest/v1/sales_orders?select=*&order=order_date.desc";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("apikey", getApiKey())
                    .header("Authorization", "Bearer " + getApiKey())
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return objectMapper.readValue(response.body(), List.class);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("error", "查詢失敗: " + e.getMessage());
        }
    }

    // =========== 2. 存檔 ===========
    @PostMapping("/api/sales")
    public Map<String, Object> createSalesOrder(@RequestBody Map<String, Object> orderData) {
        System.out.println("📦 [API模式] 收到訂單，準備存檔...");

        try {
            String orderId = "ORD" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

            // 1. 準備單頭
            Map<String, Object> headerMap = new HashMap<>();
            headerMap.put("order_id", orderId);
            headerMap.put("order_date", orderData.get("order_date"));
            headerMap.put("customer_id", orderData.get("customer_id"));
            headerMap.put("customer_address", orderData.get("customer_address"));
            headerMap.put("creator", orderData.get("creator"));
            
            sendToSupabase("sales_orders", headerMap);

            // 2. 準備單身
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (items != null) {
                for (Map<String, Object> item : items) {
                    item.put("order_id", orderId);
                    sendToSupabase("sales_order_items", item);
                }
            }
            return Map.of("success", true, "message", "存檔成功！", "orderId", orderId);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "錯誤: " + e.getMessage());
        }
    }

    // =========== 共用工具 ===========
    private void sendToSupabase(String tableName, Map<String, Object> data) throws Exception {
        String targetUrl = SUPABASE_URL + "/rest/v1/" + tableName;
        String jsonBody = objectMapper.writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("apikey", getApiKey())
                .header("Authorization", "Bearer " + getApiKey())
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 201 && response.statusCode() != 200) {
            throw new RuntimeException("Supabase 回傳錯誤 (" + response.statusCode() + "): " + response.body());
        }
        System.out.println("✅ 資料寫入 " + tableName + " 成功！");
    }
    
    private String getApiKey() {
        return "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR4b3d2d3FrcnV6YW14YXh6b3NjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkyNTE3NDMsImV4cCI6MjA4NDgyNzc0M30.rppLU_6pNc0qvkdTBi1Zv2xZI_zUlcKet1r2lsZX1nY"; 
    }
}