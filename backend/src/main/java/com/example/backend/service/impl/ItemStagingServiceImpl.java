package com.example.backend.service.impl;

import com.example.backend.dto.ItemStagingDto;
import com.example.backend.model.ItemStaging;
import com.example.backend.repository.ItemStagingRepository;
import com.example.backend.service.ItemStagingService;
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
public class ItemStagingServiceImpl implements ItemStagingService {
    
    private final ItemStagingRepository repository;
    
    @Override
    public List<ItemStagingDto> getItemsBySheetId(String sheetId) {
        return repository.findBySheetId(sheetId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public ItemStagingDto saveItem(ItemStagingDto dto) {
        ItemStaging entity = toEntity(dto);
        entity = repository.save(entity);
        return toDto(entity);
    }
    
    @Override
    public List<ItemStagingDto> saveItems(List<ItemStagingDto> dtos) {
        List<ItemStaging> entities = dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
        entities = repository.saveAll(entities);
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Override
    public void approveRow(Long id, String approverUsername) {
        ItemStaging item = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
        
        item.setApproved(true);
        item.setApprovedBy(approverUsername);
        item.setApprovedAt(LocalDateTime.now());
        item.setStatus("APPROVED");
        repository.save(item);
        
        log.info("✓ Approved item {} by {}", item.getItemName(), approverUsername);
    }
    
    @Override
    public void approveAllRows(String sheetId, String approverUsername) {
        if (sheetId == null || sheetId.isEmpty()) {
            throw new IllegalArgumentException("SheetId cannot be null or empty");
        }
        if (approverUsername == null || approverUsername.isEmpty()) {
            throw new IllegalArgumentException("Approver username cannot be null or empty");
        }
        
        List<ItemStaging> items = repository.findBySheetId(sheetId);
        if (items.isEmpty()) {
            log.warn("No items found for sheetId: {}", sheetId);
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        items.forEach(item -> {
            item.setApproved(true);
            item.setApprovedBy(approverUsername);
            item.setApprovedAt(now);
            item.setStatus("APPROVED");
        });
        repository.saveAll(items);
        
        log.info("✓ Bulk approved {} items for sheet {} by {}", items.size(), sheetId, approverUsername);
    }
    
    @Override
    public boolean areAllRowsApproved(String sheetId) {
        List<ItemStaging> items = repository.findBySheetId(sheetId);
        return !items.isEmpty() && items.stream().allMatch(i -> Boolean.TRUE.equals(i.getApproved()));
    }
    
    @Override
    public void deleteBySheetId(String sheetId) {
        repository.deleteBySheetId(sheetId);
    }
    
    private ItemStagingDto toDto(ItemStaging entity) {
        return ItemStagingDto.builder()
                .id(entity.getId())
                .sheetId(entity.getSheetId())
                .itemName(entity.getItemName())
                .itemCategory(entity.getItemCategory())
                .price(entity.getPrice())
                .quantity(entity.getQuantity())
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
    
    private ItemStaging toEntity(ItemStagingDto dto) {
        return ItemStaging.builder()
                .id(dto.getId())
                .sheetId(dto.getSheetId())
                .itemName(dto.getItemName())
                .itemCategory(dto.getItemCategory())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
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

