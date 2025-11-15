package com.example.backend.config;

import com.example.backend.model.Item;
import com.example.backend.model.Plan;
import com.example.backend.model.Product;
import com.example.backend.repository.ItemRepository;
import com.example.backend.repository.PlanRepository;
import com.example.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MockDataInitializer {

    @Bean
    public CommandLineRunner initMockData(
            ProductRepository productRepository,
            PlanRepository planRepository,
            ItemRepository itemRepository) {
        return args -> {
            // Only initialize if tables are empty
            if (productRepository.count() == 0) {
                log.info("Initializing mock product data...");
                
                productRepository.save(Product.builder()
                        .sheetId("MASTER")
                        .productName("Life Insurance Premium")
                        .rate(500.00)
                        .api("LIFE_INS_API")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                productRepository.save(Product.builder()
                        .sheetId("MASTER")
                        .productName("Health Insurance Basic")
                        .rate(750.00)
                        .api("HEALTH_INS_API")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                productRepository.save(Product.builder()
                        .sheetId("MASTER")
                        .productName("Auto Insurance Standard")
                        .rate(1200.00)
                        .api("AUTO_INS_API")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                productRepository.save(Product.builder()
                        .sheetId("MASTER")
                        .productName("Home Insurance Comprehensive")
                        .rate(2000.00)
                        .api("HOME_INS_API")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                productRepository.save(Product.builder()
                        .sheetId("MASTER")
                        .productName("Travel Insurance Annual")
                        .rate(300.00)
                        .api("TRAVEL_INS_API")
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                log.info("Created 5 mock products");
            }
            
            if (planRepository.count() == 0) {
                log.info("Initializing mock plan data...");
                
                planRepository.save(Plan.builder()
                        .sheetId("MASTER")
                        .planName("Bronze Plan")
                        .planType("BASIC")
                        .premium(299.99)
                        .coverageAmount(50000)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                planRepository.save(Plan.builder()
                        .sheetId("MASTER")
                        .planName("Silver Plan")
                        .planType("STANDARD")
                        .premium(499.99)
                        .coverageAmount(100000)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                planRepository.save(Plan.builder()
                        .sheetId("MASTER")
                        .planName("Gold Plan")
                        .planType("PREMIUM")
                        .premium(799.99)
                        .coverageAmount(250000)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                planRepository.save(Plan.builder()
                        .sheetId("MASTER")
                        .planName("Platinum Plan")
                        .planType("LUXURY")
                        .premium(1299.99)
                        .coverageAmount(500000)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                planRepository.save(Plan.builder()
                        .sheetId("MASTER")
                        .planName("Diamond Plan")
                        .planType("ULTIMATE")
                        .premium(2499.99)
                        .coverageAmount(1000000)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                log.info("Created 5 mock plans");
            }
            
            if (itemRepository.count() == 0) {
                log.info("Initializing mock item data...");
                
                itemRepository.save(Item.builder()
                        .sheetId("MASTER")
                        .itemName("Medical Equipment - X-Ray Machine")
                        .itemCategory("MEDICAL")
                        .price(150000.00)
                        .quantity(2)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                itemRepository.save(Item.builder()
                        .sheetId("MASTER")
                        .itemName("Office Furniture - Desk Set")
                        .itemCategory("FURNITURE")
                        .price(5000.00)
                        .quantity(10)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                itemRepository.save(Item.builder()
                        .sheetId("MASTER")
                        .itemName("Computer Hardware - Workstation")
                        .itemCategory("ELECTRONICS")
                        .price(2500.00)
                        .quantity(15)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                itemRepository.save(Item.builder()
                        .sheetId("MASTER")
                        .itemName("Vehicle - Company Car")
                        .itemCategory("TRANSPORT")
                        .price(35000.00)
                        .quantity(5)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                itemRepository.save(Item.builder()
                        .sheetId("MASTER")
                        .itemName("Safety Equipment - Fire Extinguisher")
                        .itemCategory("SAFETY")
                        .price(150.00)
                        .quantity(50)
                        .effectiveDate(LocalDate.of(2024, 1, 1))
                        .status("APPROVED")
                        .build());
                
                log.info("Created 5 mock items");
            }
            
            log.info("Mock data initialization complete");
        };
    }
}

