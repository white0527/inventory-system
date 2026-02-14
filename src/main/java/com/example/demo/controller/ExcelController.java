package com.example.demo.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
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

    // === 1. 匯出 Excel (配合老闆新格式) ===
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportToExcel() throws IOException {
        List<Product> products = productRepository.findAll();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("商品庫存表");

        Row headerRow = sheet.createRow(0);
        // 對齊老闆專屬 8 欄位
        String[] columns = {"商品代號", "車種", "名稱", "單位", "原廠代號", "庫存量", "車行價", "零售價"};
        
        for (int i = 0; i < columns.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(columns[i]);
            CellStyle style = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
            cell.setCellStyle(style);
        }

        int rowNum = 1;
        for (Product p : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(p.getCode());
            row.createCell(1).setCellValue(p.getCarModel());
            row.createCell(2).setCellValue(p.getName());
            row.createCell(3).setCellValue(p.getUnit());
            row.createCell(4).setCellValue(p.getOriginalCode());
            row.createCell(5).setCellValue(p.getStock() != null ? p.getStock() : 0);
            row.createCell(6).setCellValue(p.getPricePeer() != null ? p.getPricePeer() : 0);
            row.createCell(7).setCellValue(p.getPriceRetail() != null ? p.getPriceRetail() : 0);
        }

        for (int i = 0; i < columns.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        workbook.write(out);
        workbook.close();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=inventory_backup.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(out.toByteArray());
    }

    // === 2. 匯入 Excel (精準對齊 A-H 欄) ===
    @PostMapping("/import")
    public Map<String, Object> importFromExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int count = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳過標題
                
                // A 欄：商品代號
                Cell codeCell = row.getCell(0);
                if (codeCell == null || getCellValueAsString(codeCell).isEmpty()) continue;

                String code = getCellValueAsString(codeCell);
                Product p = productRepository.findById(code).orElse(new Product());
                p.setCode(code);
                
                // B 欄：車種
                if(row.getCell(1) != null) p.setCarModel(getCellValueAsString(row.getCell(1)));
                // C 欄：名稱
                if(row.getCell(2) != null) p.setName(getCellValueAsString(row.getCell(2)));
                // D 欄：單位
                if(row.getCell(3) != null) p.setUnit(getCellValueAsString(row.getCell(3)));
                // E 欄：原廠代號
                if(row.getCell(4) != null) p.setOriginalCode(getCellValueAsString(row.getCell(4)));
                // F 欄：庫存量
                if(row.getCell(5) != null) p.setStock(getCellValueAsInt(row.getCell(5)));
                // G 欄：車行價 (三價)
                if(row.getCell(6) != null) p.setPricePeer(getCellValueAsInt(row.getCell(6)));
                // H 欄：零售價 (四價)
                if(row.getCell(7) != null) p.setPriceRetail(getCellValueAsInt(row.getCell(7)));
                
                // 成本預設填 0
                p.setPriceCost(0);

                productRepository.save(p);
                count++;
            }
            return Map.of("success", true, "message", "老闆，已成功匯入 " + count + " 筆商品！");

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "匯入失敗，請檢查檔案格式: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell).trim();
    }

    private int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else {
                String val = getCellValueAsString(cell).replaceAll("[^0-9]", "");
                return val.isEmpty() ? 0 : Integer.parseInt(val);
            }
        } catch (Exception e) {
            return 0;
        }
    }
}