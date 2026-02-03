package com.example.demo.controller;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ProductController {

    // ✅ 請確認這是正確的網址 (有 'x')
    private static final String SUPABASE_URL = "https://txowvwqkruzamxaxzosc.supabase.co";
    // ✅ 請確認這是您的 Key
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR4b3d2d3FrcnV6YW14YXh6b3NjIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjkyNTE3NDMsImV4cCI6MjA4NDgyNzc0M30.rppLU_6pNc0qvkdTBi1Zv2xZI_zUlcKet1r2lsZX1nY";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 📤 Excel 匯入 API
    @PostMapping("/api/products/import")
    public ResponseEntity<String> uploadExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("請選擇一個 Excel 檔案！");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0); // 讀取第一個工作表
            int successCount = 0;

            // 從第 1 列開始讀 (第 0 列通常是標題，跳過)
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                // 讀取 Excel 欄位 (A=代號, B=名稱, C=車種, D=單價)
                String code = getCellValue(row.getCell(0));
                String name = getCellValue(row.getCell(1));
                String carType = getCellValue(row.getCell(2));
                String priceStr = getCellValue(row.getCell(3));

                if (code.isEmpty() || name.isEmpty()) continue; // 略過空行

                // 準備資料
                Map<String, Object> productData = new HashMap<>();
                productData.put("product_id", code);
                productData.put("product_name", name);
                productData.put("car_type", carType);
                try {
                    productData.put("price", Double.parseDouble(priceStr));
                } catch (NumberFormatException e) {
                    productData.put("price", 0);
                }

                // 傳送到 Supabase (表格名稱 products)
                sendToSupabase("products", productData);
                successCount++;
            }

            return ResponseEntity.ok("匯入成功！共新增 " + successCount + " 筆商品。");

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("匯入失敗: " + e.getMessage());
        }
    }

    // 工具：讀取 Excel 格子內容
    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        // 簡單判斷類型
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((int) cell.getNumericCellValue());
        } else {
            return cell.getStringCellValue();
        }
    }

    // 工具：傳送資料到 Supabase
    private void sendToSupabase(String tableName, Map<String, Object> data) throws Exception {
        String targetUrl = SUPABASE_URL + "/rest/v1/" + tableName;
        String jsonBody = objectMapper.writeValueAsString(data);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .header("apikey", API_KEY)
                .header("Authorization", "Bearer " + API_KEY)
                .header("Content-Type", "application/json")
                .header("Prefer", "return=minimal") 
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }
}