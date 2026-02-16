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
    private JdbcTemplate jdbcTemplate; // 使用此工具執行 BatchUpdate

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            List<Product> products = new ArrayList<>();
            LocalDateTime now = LocalDateTime.now(); // 關鍵：同步用的時間印章

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; 
                Product p = new Product();
                p.setCode(getCellValue(row.getCell(0)));
                p.setName(getCellValue(row.getCell(1)));
                p.setCarModel(getCellValue(row.getCell(2)));
                p.setPricePeer(Double.parseDouble(getCellValue(row.getCell(3))));
                p.setPriceRetail(Double.parseDouble(getCellValue(row.getCell(4))));
                p.setStock(Integer.parseInt(getCellValue(row.getCell(5))));
                p.setUpdatedAt(now); 
                products.add(p);
            }

            // 批次寫入：三萬筆資料每 1000 筆一組快速塞入
            String sql = "INSERT INTO products (code, name, car_model, price_peer, price_retail, stock, updated_at) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                         "ON CONFLICT (code) DO UPDATE SET " +
                         "name = EXCLUDED.name, car_model = EXCLUDED.car_model, " +
                         "price_peer = EXCLUDED.price_peer, price_retail = EXCLUDED.price_retail, " +
                         "stock = EXCLUDED.stock, updated_at = EXCLUDED.updated_at";

            jdbcTemplate.batchUpdate(sql, products, 1000, (ps, product) -> {
                ps.setString(1, product.getCode());
                ps.setString(2, product.getName());
                ps.setString(3, product.getCarModel());
                ps.setDouble(4, product.getPricePeer());
                ps.setDouble(5, product.getPriceRetail());
                ps.setInt(6, product.getStock());
                ps.setObject(7, product.getUpdatedAt());
            });

            return "成功匯入 " + products.size() + " 筆零件！";
        } catch (Exception e) {
            return "匯入失敗：" + e.getMessage();
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "0";
        return cell.getCellType() == CellType.NUMERIC ? 
               String.valueOf((long)cell.getNumericCellValue()) : cell.getStringCellValue().trim();
    }
}