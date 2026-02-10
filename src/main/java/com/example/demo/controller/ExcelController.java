package com.example.demo.controller;

import com.example.demo.entity.Product; // 如果您的 Product 在 model 包，請改為 com.example.demo.model.Product
import com.example.demo.repository.ProductRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*")
public class ExcelController {

    @Autowired
    private ProductRepository productRepository;

    // === 1. 匯出 Excel (下載) ===
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        List<Product> products = productRepository.findAll();

        // 建立 Excel 活頁簿
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品庫存表");

        // 建立標題列
        Row headerRow = sheet.createRow(0);
        String[] columns = {"商品代號", "商品名稱", "車種", "庫存", "儲位", "散客價", "同業價", "成本"};
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        // 填入資料
        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getCode());
            row.createCell(1).setCellValue(p.getName());
            row.createCell(2).setCellValue(p.getCarModel());
            row.createCell(3).setCellValue(p.getStock() != null ? p.getStock() : 0);
            row.createCell(4).setCellValue(p.getLocation());
            row.createCell(5).setCellValue(p.getPriceRetail() != null ? p.getPriceRetail() : 0);
            row.createCell(6).setCellValue(p.getPricePeer() != null ? p.getPricePeer() : 0);
            row.createCell(7).setCellValue(p.getPriceCost() != null ? p.getPriceCost() : 0);
        }

        // 自動調整欄寬
        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        // 轉成 Byte Array 輸出
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=products.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    // === 2. 匯入 Excel (上傳) ===
    @PostMapping("/import")
    public Map<String, Object> importFromExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            // 從第 1 列開始讀 (略過標題列)
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; 
                
                // 如果代號是空的，就跳過
                Cell codeCell = row.getCell(0);
                if (codeCell == null || codeCell.getStringCellValue().trim().isEmpty()) continue;

                String code = codeCell.getStringCellValue();
                
                // 嘗試找現有商品，沒有就新建
                Product p = productRepository.findById(code).orElse(new Product());
                p.setCode(code);
                
                // 讀取各欄位 (注意處理空值)
                if(row.getCell(1) != null) p.setName(getCellValueAsString(row.getCell(1)));
                if(row.getCell(2) != null) p.setCarModel(getCellValueAsString(row.getCell(2)));
                if(row.getCell(3) != null) p.setStock((int) row.getCell(3).getNumericCellValue());
                if(row.getCell(4) != null) p.setLocation(getCellValueAsString(row.getCell(4)));
                if(row.getCell(5) != null) p.setPriceRetail((int) row.getCell(5).getNumericCellValue());
                if(row.getCell(6) != null) p.setPricePeer((int) row.getCell(6).getNumericCellValue());
                if(row.getCell(7) != null) p.setPriceCost((int) row.getCell(7).getNumericCellValue());

                productRepository.save(p);
                count++;
            }
            return Map.of("success", true, "message", "成功匯入 " + count + " 筆資料");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "匯入失敗: " + e.getMessage());
        }
    }

    // 輔助方法：把 Excel 格子轉成字串 (避免數字被讀成文字報錯)
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC: return String.valueOf((int)cell.getNumericCellValue()); // 假設都是整數
            default: return "";
        }
    }
}