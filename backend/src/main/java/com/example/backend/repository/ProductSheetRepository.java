package com.example.backend.repository;

import com.example.backend.model.ProductSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductSheetRepository extends JpaRepository<ProductSheet, Long> {
    Optional<ProductSheet> findBySheetId(String sheetId);
    Optional<ProductSheet> findByProcessInstanceId(String processInstanceId);
}

