package com.example.backend.repository;

import com.example.backend.model.ItemStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemStagingRepository extends JpaRepository<ItemStaging, Long> {
    List<ItemStaging> findBySheetId(String sheetId);
    void deleteBySheetId(String sheetId);
}

