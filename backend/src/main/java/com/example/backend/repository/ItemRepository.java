package com.example.backend.repository;

import com.example.backend.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findBySheetId(String sheetId);
    List<Item> findByStatus(String status);
    List<Item> findBySheetIdAndStatus(String sheetId, String status);
}

