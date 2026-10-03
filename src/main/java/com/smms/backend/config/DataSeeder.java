package com.smms.backend.config;

import com.smms.backend.model.Product;
import com.smms.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Seeds global default product templates (user_id IS NULL) on application startup.
 * These templates are used to populate new users' inventories.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        seedDefaultTemplates();
    }

    private void seedDefaultTemplates() {
        // Check if global templates (user_id is null) already exist
        if (productRepository.findByUserIsNullAndIsDefaultTrue().isEmpty()) {
            List<Product> templates = new ArrayList<>();

            // 1. Dairy
            templates.add(createTemplate("Milk 1L", "DEF-MILK-1L", "Dairy", "Milk Products", 65.0, 60.0, "5"));
            templates.add(createTemplate("Amul Butter 100g", "DEF-BUTTER-100", "Dairy", "Butter & Ghee", 58.0, 55.0, "12"));
            templates.add(createTemplate("Paneer 200g", "DEF-PANEER-200", "Dairy", "Paneer", 90.0, 85.0, "5"));

            // 2. Bakery
            templates.add(createTemplate("Brown Bread", "DEF-BREAD-BROWN", "Bakery", "Bread", 45.0, 40.0, "0"));
            templates.add(createTemplate("Marie Biscuits", "DEF-MARIE", "Bakery", "Cookies & Biscuits", 30.0, 28.0, "18"));

            // 3. Snacks
            templates.add(createTemplate("Lays Classic", "DEF-LAYS-CL", "Snacks", "Chips", 20.0, 20.0, "12"));
            templates.add(createTemplate("Cadbury Silk", "DEF-SILK", "Snacks", "Chocolates", 80.0, 75.0, "18"));

            // 4. Staples (Stretches categories a bit)
            templates.add(createTemplate("Atta 5kg", "DEF-ATTA-5K", "Fresh Produce", "Flour & Atta", 240.0, 225.0, "0"));
            templates.add(createTemplate("Cooking Oil 1L", "DEF-OIL-1L", "Fresh Produce", "Oils", 160.0, 150.0, "5"));

            productRepository.saveAll(templates);
            System.out.println("Seeded " + templates.size() + " global default product templates.");
        }
    }

    private Product createTemplate(String name, String code, String cat, String sub, Double mrp, Double sale, String tax) {
        Product p = new Product();
        p.setUser(null); // Global template
        p.setItemName(name);
        p.setItemCode(code);
        p.setCategory(cat);
        p.setSubcategory(sub);
        p.setMrp(mrp);
        p.setSaleRate(sale);
        p.setQuantity(0);
        p.setExpiryDate(LocalDate.now().plusYears(1));
        p.setDefault(true);
        return p;
    }
}
