package com.example.backend.flowable;

import org.flowable.task.service.delegate.DelegateTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.example.backend.service.RetentionOfferService;
import com.example.backend.model.RetentionOffer;

@Component
public class RetentionOfferTaskListener {
    @Autowired
    private RetentionOfferService retentionOfferService;

    public void handleTaskComplete(DelegateTask delegateTask) {
        Object offerData = delegateTask.getVariable("retentionOfferData");
        if (offerData instanceof RetentionOffer) {
            retentionOfferService.createOffer((RetentionOffer) offerData);
        }
    }
}
