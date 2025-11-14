package com.example.backend.service;

import com.example.backend.dto.ItemDto;
import com.example.backend.model.Item;

import java.util.List;

public interface ItemService {
    Item createItem(ItemDto itemDto);
    Item updateItem(Long id, ItemDto itemDto);
    void deleteItem(Long id);
    Item getItem(Long id);
    List<Item> getItemsBySheet(String sheetId);
    void approveItem(Long itemId, String approvedBy);
    void rejectItem(Long itemId, String approvedBy, String comments);
    void approveAllItems(String sheetId, String approvedBy);
    List<Item> saveItemsFromTask(String sheetId, List<ItemDto> items, String editedBy);
}

