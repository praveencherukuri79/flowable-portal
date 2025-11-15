package com.example.backend.flowable;

import com.example.backend.dto.PlanStagingDto;
import com.example.backend.util.ComparisonUtils;
import com.example.backend.util.TaskListenerUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Task listener for Plan staging operations
 * Delegates to TaskListenerUtils for shared logic
 */
@Component("planTaskListener")
@RequiredArgsConstructor
@Slf4j
public class PlanTaskListener implements TaskListener {
    
    private final TaskListenerUtils taskListenerUtils;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        taskListenerUtils.processItemStaging(
            delegateTask,
            "Plan",
            new TypeReference<List<PlanStagingDto>>() {},
            this::hasPlanChanged,
            PlanStagingDto::getPlanName
        );
    }
    
    /**
     * Checks if a plan's business data has changed
     */
    private boolean hasPlanChanged(PlanStagingDto existing, PlanStagingDto incoming) {
        return !ComparisonUtils.safeEquals(existing.getPlanName(), incoming.getPlanName()) ||
               !ComparisonUtils.safeEquals(existing.getPlanType(), incoming.getPlanType()) ||
               !ComparisonUtils.safeEquals(existing.getPremium(), incoming.getPremium()) ||
               !ComparisonUtils.safeEquals(existing.getCoverageAmount(), incoming.getCoverageAmount()) ||
               !ComparisonUtils.datesEqual(existing.getEffectiveDate(), incoming.getEffectiveDate());
    }
}
