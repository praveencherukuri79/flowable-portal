package com.example.backend.service.impl;

import com.example.backend.dto.ItemDto;
import com.example.backend.model.Item;
import com.example.backend.repository.ItemRepository;
import com.example.backend.service.ItemService;
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
public class ItemServiceImpl implements ItemService {
    
    private final ItemRepository itemRepository;
    
    @Override
    @Transactional
    public Item createItem(ItemDto dto) {
        Item item = Item.builder()
                .sheetId(dto.getSheetId())
                .itemName(dto.getItemName())
                .itemCategory(dto.getItemCategory())
                .price(dto.getPrice())
                .quantity(dto.getQuantity())
                .effectiveDate(dto.getEffectiveDate())
                .status("PENDING")
                .editedBy(dto.getEditedBy())
                .editedAt(LocalDateTime.now())
                .comments(dto.getComments())
                .build();
        
        return itemRepository.save(item);
    }
    
    @Override
    @Transactional
    public Item updateItem(Long id, ItemDto dto) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
        
        item.setItemName(dto.getItemName());
        item.setItemCategory(dto.getItemCategory());
        item.setPrice(dto.getPrice());
        item.setQuantity(dto.getQuantity());
        item.setEffectiveDate(dto.getEffectiveDate());
        item.setEditedBy(dto.getEditedBy());
        item.setEditedAt(LocalDateTime.now());
        item.setComments(dto.getComments());
        
        return itemRepository.save(item);
    }
    
    @Override
    @Transactional
    public void deleteItem(Long id) {
        itemRepository.deleteById(id);
    }
    
    @Override
    public Item getItem(Long id) {
        return itemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Item not found: " + id));
    }
    
    @Override
    public List<Item> getItemsBySheet(String sheetId) {
        return itemRepository.findBySheetId(sheetId);
    }
    
    @Override
    @Transactional
    public void approveItem(Long itemId, String approvedBy) {
        Item item = getItem(itemId);
        item.setStatus("APPROVED");
        item.setApprovedBy(approvedBy);
        item.setApprovedAt(LocalDateTime.now());
        itemRepository.save(item);
        
        log.info("Item {} approved by {}", itemId, approvedBy);
    }
    
    @Override
    @Transactional
    public void rejectItem(Long itemId, String approvedBy, String comments) {
        Item item = getItem(itemId);
        item.setStatus("REJECTED");
        item.setApprovedBy(approvedBy);
        item.setApprovedAt(LocalDateTime.now());
        item.setComments(comments);
        itemRepository.save(item);
        
        log.info("Item {} rejected by {}", itemId, approvedBy);
    }
    
    @Override
    @Transactional
    public void approveAllItems(String sheetId, String approvedBy) {
        List<Item> items = itemRepository.findBySheetIdAndStatus(sheetId, "PENDING");
        
        items.forEach(item -> {
            item.setStatus("APPROVED");
            item.setApprovedBy(approvedBy);
            item.setApprovedAt(LocalDateTime.now());
        });
        
        itemRepository.saveAll(items);
        
        log.info("Approved all {} items for sheet {} by {}", items.size(), sheetId, approvedBy);
    }
    
    @Override
    @Transactional
    public List<Item> saveItemsFromTask(String sheetId, List<ItemDto> itemDtos, String editedBy) {
        // Delete existing items for this sheet
        List<Item> existingItems = itemRepository.findBySheetId(sheetId);
        itemRepository.deleteAll(existingItems);
        
        // Create new items
        List<Item> items = itemDtos.stream()
                .map(dto -> Item.builder()
                        .sheetId(sheetId)
                        .itemName(dto.getItemName())
                        .itemCategory(dto.getItemCategory())
                        .price(dto.getPrice())
                        .quantity(dto.getQuantity())
                        .effectiveDate(dto.getEffectiveDate())
                        .status("PENDING")
                        .editedBy(editedBy)
                        .editedAt(LocalDateTime.now())
                        .comments(dto.getComments())
                        .build())
                .collect(Collectors.toList());
        
        List<Item> savedItems = itemRepository.saveAll(items);
        
        log.info("Saved {} items for sheet {} by {}", savedItems.size(), sheetId, editedBy);
        
        return savedItems;
    }
}

