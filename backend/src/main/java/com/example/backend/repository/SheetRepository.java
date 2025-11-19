package com.example.backend.repository;

import com.example.backend.model.Sheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SheetRepository extends JpaRepository<Sheet, Long> {
    
    Optional<Sheet> findBySheetId(String sheetId);
    
    /**
     * Find the latest sheet (highest version) for a processInstanceId + sheetType combination
     */
    Optional<Sheet> findFirstByProcessInstanceIdAndSheetTypeOrderByVersionDesc(String processInstanceId, String sheetType);
}

