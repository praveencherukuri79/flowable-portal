package com.example.backend.repository;

import com.example.backend.model.ProductStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductStagingRepository extends JpaRepository<ProductStaging, Long> {
    List<ProductStaging> findBySheetId(String sheetId);
    void deleteBySheetId(String sheetId);
}

