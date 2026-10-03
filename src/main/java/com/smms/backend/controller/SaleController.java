package com.smms.backend.controller;

import com.smms.backend.model.Sale;
import com.smms.backend.service.SaleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@CrossOrigin(origins = "http://localhost:5173")
public class SaleController {

    @Autowired
    private SaleService saleService;

    @PostMapping
    public ResponseEntity<?> createSale(@RequestBody Sale sale) {
        try {
            Sale savedSale = saleService.createSale(sale);
            return ResponseEntity.ok(savedSale);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<java.util.List<Sale>> getAllSales() {
        java.util.List<Sale> sales = saleService.getAllSales();
        System.out.println("DEBUG: Sending " + sales.size() + " sales to frontend.");
        return ResponseEntity.ok(sales);
    }

    @GetMapping("/daily")
    public ResponseEntity<Double> getTodaySales() {
        Double todaySales = saleService.getTodaySales();
        return ResponseEntity.ok(todaySales);
    }

    @GetMapping("/next-transaction-id")
    public ResponseEntity<String> getNextTransactionId() {
        String nextId = saleService.getNextTransactionId();
        return ResponseEntity.ok(nextId);
    }
}
