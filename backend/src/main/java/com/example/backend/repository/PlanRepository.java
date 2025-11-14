package com.example.backend.repository;

import com.example.backend.model.Plan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findBySheetId(String sheetId);
    List<Plan> findByStatus(String status);
    List<Plan> findBySheetIdAndStatus(String sheetId, String status);
}

