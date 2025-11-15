package com.example.backend.service.impl;

import com.example.backend.dto.ProductStagingDto;
import com.example.backend.model.ProductStaging;
import com.example.backend.repository.ProductStagingRepository;
import com.example.backend.service.ProductStagingService;
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
@Transactional
public class ProductStagingServiceImpl implements ProductStagingService {
    
    private final ProductStagingRepository repository;
    
    @Override
    public List<ProductStagingDto> getProductsBySheetId(String sheetId) {
        return repository.findBySheetId(sheetId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ProductStagingDto saveProduct(ProductStagingDto dto) {
        ProductStaging entity = toEntity(dto);
        entity = repository.save(entity);
        return toDto(entity);
    }
    
    @Override
    public List<ProductStagingDto> saveProducts(List<ProductStagingDto> dtos) {
        List<ProductStaging> entities = dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        entities = repository.saveAll(entities);
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void approveRow(Long id, String approverUsername) {
        ProductStaging product = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        
        product.setApproved(true);
        product.setApprovedBy(approverUsername);
        product.setApprovedAt(LocalDateTime.now());
        product.setStatus("APPROVED");
        repository.save(product);
        
        log.info("✓ Approved product {} by {}", product.getProductName(), approverUsername);
    }
    
    @Override
    public void approveAllRows(String sheetId, String approverUsername) {
        if (sheetId == null || sheetId.isEmpty()) {
            throw new IllegalArgumentException("SheetId cannot be null or empty");
        }
        if (approverUsername == null || approverUsername.isEmpty()) {
            throw new IllegalArgumentException("Approver username cannot be null or empty");
        }
        
        List<ProductStaging> products = repository.findBySheetId(sheetId);
        if (products.isEmpty()) {
            log.warn("No products found for sheetId: {}", sheetId);
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        products.forEach(product -> {
            product.setApproved(true);
            product.setApprovedBy(approverUsername);
            product.setApprovedAt(now);
            product.setStatus("APPROVED");
        });
        repository.saveAll(products);
        
        log.info("✓ Bulk approved {} products for sheet {} by {}", products.size(), sheetId, approverUsername);
    }
    
    @Override
    public boolean areAllRowsApproved(String sheetId) {
        List<ProductStaging> products = repository.findBySheetId(sheetId);
        return !products.isEmpty() && products.stream().allMatch(p -> Boolean.TRUE.equals(p.getApproved()));
    }
    
    @Override
    public void deleteBySheetId(String sheetId) {
        repository.deleteBySheetId(sheetId);
    }
    
    private ProductStagingDto toDto(ProductStaging entity) {
        return ProductStagingDto.builder()
                .id(entity.getId())
                .sheetId(entity.getSheetId())
                .productName(entity.getProductName())
                .rate(entity.getRate())
                .api(entity.getApi())
                .effectiveDate(entity.getEffectiveDate())
                .status(entity.getStatus())
                .approved(entity.getApproved())
                .approvedBy(entity.getApprovedBy())
                .approvedAt(entity.getApprovedAt())
                .editedBy(entity.getEditedBy())
                .editedAt(entity.getEditedAt())
                .comments(entity.getComments())
                .build();
    }
    
    private ProductStaging toEntity(ProductStagingDto dto) {
        return ProductStaging.builder()
                .id(dto.getId())
                .sheetId(dto.getSheetId())
                .productName(dto.getProductName())
                .rate(dto.getRate())
                .api(dto.getApi())
                .effectiveDate(dto.getEffectiveDate())
                .status(dto.getStatus())
                .approved(dto.getApproved())
                .approvedBy(dto.getApprovedBy())
                .approvedAt(dto.getApprovedAt())
                .editedBy(dto.getEditedBy())
                .editedAt(dto.getEditedAt())
                .comments(dto.getComments())
                .build();
    }
}

