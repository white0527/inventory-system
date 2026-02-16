package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Product;

@RestController
@RequestMapping("/api/excel")
@CrossOrigin(origins = "*")
public class ExcelController {

    @Autowired
    private JdbcTemplate jdbcTemplate; // 使用 JdbcTemplate 進行高效率 BatchUpdate

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Product> products = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now(); // 關鍵：讓手機 App 識別更新的時間戳記

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳過第一列標題
                
                // 檢查關鍵欄位是否為空，避免解析錯誤
                if (row.getCell(0) == null) continue;

                Product p = new Product();
                p.setCode(getCellValue(row.getCell(0)));
                p.setName(getCellValue(row.getCell(1)));
                p.setCarModel(getCellValue(row.getCell(2)));
                
                // 數值解析強化：防止 Excel 欄位格式不正確導致崩潰
                p.setPricePeer(parseSafeDouble(getCellValue(row.getCell(3))));
                p.setPriceRetail(parseSafeDouble(getCellValue(row.getCell(4))));
                p.setStock(parseSafeInt(getCellValue(row.getCell(5))));
                p.setUpdatedAt(now); 
                products.add(p);
            }

            // --- 核心優化：對齊資料庫中文欄位名稱 ---
            // 注意：PostgreSQL 識別中文欄位必須使用 \"雙引號\"
            String sql = "INSERT INTO products (\"商品代號\", \"名稱\", \"車種\", \"車行價\", \"零售價\", \"庫存\", \"updated_at\") " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT (\"商品代號\") DO UPDATE SET " +
                         "\"名稱\" = EXCLUDED.\"名稱\", " +
                         "\"車種\" = EXCLUDED.\"車種\", " +
                         "\"車行價\" = EXCLUDED.\"車行價\", " +
                         "\"零售價\" = EXCLUDED.\"零售價\", " +
                         "\"庫存\" = EXCLUDED.\"庫存\", " +
                         "\"updated_at\" = EXCLUDED.\"updated_at\"";

            // 執行批次寫入：每 1000 筆資料發送一次，極速存檔不中斷
            jdbcTemplate.batchUpdate(sql, products, 1000, (ps, product) -> {
                ps.setString(1, product.getCode());
                ps.setString(2, product.getName());
                ps.setString(3, product.getCarModel());
                ps.setDouble(4, product.getPricePeer());
                ps.setDouble(5, product.getPriceRetail());
                ps.setInt(6, product.getStock());
                ps.setObject(7, product.getUpdatedAt()); // 寫入時間印章供手機同步使用
            });

            return "✅ 成功匯入 " + products.size() + " 筆零件！三萬筆資料已同步更新。";
        } catch (Exception e) {
            return "❌ 匯入失敗：" + e.getMessage();
        }
    }

    // 安全解析 Double：防止價格欄位有非數字內容
    private Double parseSafeDouble(String val) {
        try {
            return Double.parseDouble(val);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // 安全解析 Integer：防止庫存欄位有非數字內容
    private Integer parseSafeInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC) {
            return String.valueOf((long)cell.getNumericCellValue());
        }
        return cell.getStringCellValue().trim();
    }
}