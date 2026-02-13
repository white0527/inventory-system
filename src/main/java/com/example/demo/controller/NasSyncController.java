package com.example.demo.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.entity.Product;
import com.example.demo.repository.ProductRepository;

@RestController
@RequestMapping("/api/nas")
@CrossOrigin(origins = "*")
public class NasSyncController {

    @Autowired
    private ProductRepository productRepository;

    // 從 application.properties 讀取路徑
    @Value("${app.nas.file.path}")
    private String nasFilePath;

    @Value("${app.nas.report.path}")
    private String reportPath;

    @PostMapping("/sync")
    public Map<String, Object> syncFromNas() {
        File file = new File(nasFilePath);
        if (!file.exists()) {
            return Map.of("success", false, "message", "找不到 NAS 檔案，請確認路徑: " + nasFilePath);
        }

        List<String> changeLogs = new ArrayList<>();
        int updateCount = 0;
        int newCount = 0;

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            
            // 讀取 Excel 每一列
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // 跳過標題

                String code = getCellValue(row.getCell(0));
                if (code.isEmpty()) continue;

                String name = getCellValue(row.getCell(1));
                int stock = (int) getNumericValue(row.getCell(3));
                int priceRetail = (int) getNumericValue(row.getCell(5));
                int pricePeer = (int) getNumericValue(row.getCell(6));
                int priceCost = (int) getNumericValue(row.getCell(7));
                String location = getCellValue(row.getCell(4));
                String carModel = getCellValue(row.getCell(2));

                // 查詢資料庫現有資料
                Optional<Product> opt = productRepository.findById(code);
                Product p;
                
                if (opt.isPresent()) {
                    p = opt.get();
                    // 比對是否有變更 (這裡只示範比對庫存和價格，您可以自行增加)
                    boolean changed = false;
                    String log = "商品 " + code + " (" + name + "): ";

                    if (!Objects.equals(p.getStock(), stock)) {
                        log += "庫存 " + p.getStock() + "->" + stock + "; ";
                        p.setStock(stock);
                        changed = true;
                    }
                    if (!Objects.equals(p.getPriceRetail(), priceRetail)) {
                        log += "售價 " + p.getPriceRetail() + "->" + priceRetail + "; ";
                        p.setPriceRetail(priceRetail);
                        changed = true;
                    }
                    
                    // 如果有變更，加入紀錄
                    if (changed) {
                        changeLogs.add(log);
                        updateCount++;
                    }
                    
                    // 無論有無變更，都更新其他欄位以確保同步
                    p.setName(name);
                    p.setLocation(location);
                    p.setCarModel(carModel);
                    p.setPricePeer(pricePeer);
                    p.setPriceCost(priceCost);
                    
                } else {
                    // 新商品
                    p = new Product();
                    p.setCode(code);
                    p.setName(name);
                    p.setStock(stock);
                    p.setPriceRetail(priceRetail);
                    p.setPricePeer(pricePeer);
                    p.setPriceCost(priceCost);
                    p.setLocation(location);
                    p.setCarModel(carModel);
                    
                    changeLogs.add("新增商品: " + code + " " + name);
                    newCount++;
                }
                
                // 存入 Supabase
                productRepository.save(p);
            }

            // 如果有變動，產生 Excel 明細表存回 NAS
            if (!changeLogs.isEmpty()) {
                generateChangeReport(changeLogs);
            }

            return Map.of(
                "success", true, 
                "message", "同步完成！新增: " + newCount + " 筆, 更新: " + updateCount + " 筆",
                "logs", changeLogs
            );

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("success", false, "message", "同步失敗: " + e.getMessage());
        }
    }

    // 產生異動明細表
    private void generateChangeReport(List<String> logs) throws IOException {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("異動明細");
        
        // 標題
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("異動時間");
        header.createCell(1).setCellValue("內容");

        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        int rowIdx = 1;
        for (String log : logs) {
            Row r = sheet.createRow(rowIdx++);
            r.createCell(0).setCellValue(time);
            r.createCell(1).setCellValue(log);
        }
        
        sheet.autoSizeColumn(0);
        sheet.autoSizeColumn(1);

        // 確保目錄存在
        File dir = new File(reportPath);
        if (!dir.exists()) dir.mkdirs();

        // 檔名加上日期
        String fileName = "ChangeLog_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
        try (FileOutputStream fos = new FileOutputStream(new File(dir, fileName))) {
            wb.write(fos);
        }
        wb.close();
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private double getNumericValue(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        try {
            return Double.parseDouble(cell.getStringCellValue());
        } catch (Exception e) { return 0; }
    }
}