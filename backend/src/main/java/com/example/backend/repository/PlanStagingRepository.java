package com.example.backend.repository;

import com.example.backend.model.PlanStaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanStagingRepository extends JpaRepository<PlanStaging, Long> {
    List<PlanStaging> findBySheetId(String sheetId);
    void deleteBySheetId(String sheetId);
}

