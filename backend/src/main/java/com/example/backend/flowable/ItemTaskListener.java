package com.example.backend.flowable;

import com.example.backend.dto.ItemStagingDto;
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
 * Task listener for Item staging operations
 * Delegates to TaskListenerUtils for shared logic
 */
@Component("itemTaskListener")
@RequiredArgsConstructor
@Slf4j
public class ItemTaskListener implements TaskListener {
    
    private final TaskListenerUtils taskListenerUtils;
    
    @Override
    public void notify(DelegateTask delegateTask) {
        taskListenerUtils.processItemStaging(
            delegateTask,
            "Item",
            new TypeReference<List<ItemStagingDto>>() {},
            this::hasItemChanged,
            ItemStagingDto::getItemName
        );
    }
    
    /**
     * Checks if an item's business data has changed
     */
    private boolean hasItemChanged(ItemStagingDto existing, ItemStagingDto incoming) {
        return !ComparisonUtils.safeEquals(existing.getItemName(), incoming.getItemName()) ||
               !ComparisonUtils.safeEquals(existing.getItemCategory(), incoming.getItemCategory()) ||
               !ComparisonUtils.safeEquals(existing.getPrice(), incoming.getPrice()) ||
               !ComparisonUtils.safeEquals(existing.getQuantity(), incoming.getQuantity()) ||
               !ComparisonUtils.datesEqual(existing.getEffectiveDate(), incoming.getEffectiveDate());
    }
}
