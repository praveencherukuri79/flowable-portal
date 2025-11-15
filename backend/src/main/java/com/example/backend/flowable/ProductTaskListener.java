package com.example.backend.flowable;

import com.example.backend.dto.ProductStagingDto;
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
 * Task listener for Product staging operations
 * Delegates to TaskListenerUtils for shared logic
 */
@Component("productTaskListener")
@RequiredArgsConstructor
@Slf4j
public class ProductTaskListener implements TaskListener {
    
    private final TaskListenerUtils taskListenerUtils;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        taskListenerUtils.processItemStaging(
            delegateTask,
            "Product",
            new TypeReference<List<ProductStagingDto>>() {},
            this::hasProductChanged,
            ProductStagingDto::getProductName
        );
    }
    
    /**
     * Checks if a product's business data has changed
     */
    private boolean hasProductChanged(ProductStagingDto existing, ProductStagingDto incoming) {
        return !ComparisonUtils.safeEquals(existing.getProductName(), incoming.getProductName()) ||
               !ComparisonUtils.safeEquals(existing.getRate(), incoming.getRate()) ||
               !ComparisonUtils.safeEquals(existing.getApi(), incoming.getApi()) ||
               !ComparisonUtils.datesEqual(existing.getEffectiveDate(), incoming.getEffectiveDate());
    }
}
