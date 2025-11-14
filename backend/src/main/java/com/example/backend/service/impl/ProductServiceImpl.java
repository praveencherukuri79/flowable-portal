package com.example.backend.service.impl;

import com.example.backend.dto.ProductDto;
import com.example.backend.model.Product;
import com.example.backend.repository.ProductRepository;
import com.example.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    
    private final ProductRepository productRepository;
    
    @Override
    @Transactional
    public Product createProduct(ProductDto dto) {
        Product product = Product.builder()
                .sheetId(dto.getSheetId())
                .productName(dto.getProductName())
                .rate(dto.getRate())
                .api(dto.getApi())
                .effectiveDate(dto.getEffectiveDate())
                .status("PENDING")
                .editedBy(dto.getEditedBy())
                .editedAt(LocalDateTime.now())
                .comments(dto.getComments())
                .build();
        
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public Product updateProduct(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
        
        product.setProductName(dto.getProductName());
        product.setRate(dto.getRate());
        product.setApi(dto.getApi());
        product.setEffectiveDate(dto.getEffectiveDate());
        product.setEditedBy(dto.getEditedBy());
        product.setEditedAt(LocalDateTime.now());
        product.setComments(dto.getComments());
        
        return productRepository.save(product);
    }
    
    @Override
    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
    
    @Override
    public Product getProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found: " + id));
    }
    
    @Override
    public List<Product> getProductsBySheet(String sheetId) {
        return productRepository.findBySheetId(sheetId);
    }
    
    @Override
    @Transactional
    public void approveProduct(Long productId, String approvedBy) {
        Product product = getProduct(productId);
        product.setStatus("APPROVED");
        product.setApprovedBy(approvedBy);
        product.setApprovedAt(LocalDateTime.now());
        productRepository.save(product);
        
        log.info("Product {} approved by {}", productId, approvedBy);
    }
    
    @Override
    @Transactional
    public void rejectProduct(Long productId, String approvedBy, String comments) {
        Product product = getProduct(productId);
        product.setStatus("REJECTED");
        product.setApprovedBy(approvedBy);
        product.setApprovedAt(LocalDateTime.now());
        product.setComments(comments);
        productRepository.save(product);
        
        log.info("Product {} rejected by {}", productId, approvedBy);
    }
    
    @Override
    @Transactional
    public void approveAllProducts(String sheetId, String approvedBy) {
        List<Product> products = productRepository.findBySheetIdAndStatus(sheetId, "PENDING");
        
        products.forEach(product -> {
            product.setStatus("APPROVED");
            product.setApprovedBy(approvedBy);
            product.setApprovedAt(LocalDateTime.now());
        });
        
        productRepository.saveAll(products);
        
        log.info("Approved all {} products for sheet {} by {}", 
                products.size(), sheetId, approvedBy);
    }
    
    @Override
    @Transactional
    public List<Product> saveProductsFromTask(String sheetId, List<ProductDto> productDtos, String editedBy) {
        // Delete existing products for this sheet
        List<Product> existingProducts = productRepository.findBySheetId(sheetId);
        productRepository.deleteAll(existingProducts);
        
        // Create new products
        List<Product> products = productDtos.stream()
                .map(dto -> Product.builder()
                        .sheetId(sheetId)
                        .productName(dto.getProductName())
                        .rate(dto.getRate())
                        .api(dto.getApi())
                        .effectiveDate(dto.getEffectiveDate())
                        .status("PENDING")
                        .editedBy(editedBy)
                        .editedAt(LocalDateTime.now())
                        .comments(dto.getComments())
                        .build())
                .collect(Collectors.toList());
        
        List<Product> savedProducts = productRepository.saveAll(products);
        
        log.info("Saved {} products for sheet {} by {}", 
                savedProducts.size(), sheetId, editedBy);
        
        return savedProducts;
    }
}

