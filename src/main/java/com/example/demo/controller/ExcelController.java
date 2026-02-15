package com.example.demo.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*")
public class ExcelController {

    @Autowired
    private ProductRepository productRepository;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Product> products = new ArrayList<>();

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳過標題列
                
                // 檢查關鍵欄位是否為空，避免匯入空白行
                if (row.getCell(0) == null || row.getCell(1) == null) continue;

                Product p = new Product();
                // 索引順序：0:代號, 1:名稱, 2:車種, 3:車行價, 4:零售價, 5:庫存
                p.setCode(getCellValue(row.getCell(0)));
                p.setName(getCellValue(row.getCell(1)));
                p.setCarModel(getCellValue(row.getCell(2)));
                
                // 使用優化後的數值解析，避免格式錯誤
                p.setPricePeer(parseSafeDouble(getCellValue(row.getCell(3))));
                p.setPriceRetail(parseSafeDouble(getCellValue(row.getCell(4))));
                p.setStock(parseSafeInt(getCellValue(row.getCell(5))));
                
                products.add(p);
            }

            if (!products.isEmpty()) {
                // saveAll 會觸發 Product.java 裡的 @PrePersist 自動填入更新時間
                productRepository.saveAll(products); 
                return ResponseEntity.ok(Map.of("success", true, "count", products.size()));
            }
            return ResponseEntity.badRequest().body("檔案內容為空或格式不符");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("匯入失敗: " + e.getMessage());
        }
    }

    // 安全解析 Double
    private Double parseSafeDouble(String val) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // 安全解析 Integer
    private Integer parseSafeInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return ""; // 改為空字串較安全
        if (cell.getCellType() == CellType.NUMERIC) {
            // 防止科學記號出現，直接取數值
            double numericValue = cell.getNumericCellValue();
            if (numericValue == (long) numericValue) {
                return String.valueOf((long) numericValue);
            }
            return String.valueOf(numericValue);
        }
        return cell.getStringCellValue().trim();
    }
}