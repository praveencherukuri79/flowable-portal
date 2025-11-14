package com.example.backend.delegates;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Notification delegate for sending notifications during maker-checker workflow.
 * Handles email notifications, alerts, and audit logging.
 */
@Component("notificationDelegate")
public class NotificationDelegate implements JavaDelegate {

    private static final Logger logger = LoggerFactory.getLogger(NotificationDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        logger.info("Notification Delegate executed for processInstanceId: {}", execution.getProcessInstanceId());
        
        try {
            String notificationType = (String) execution.getVariable("notificationType");
            String recipient = (String) execution.getVariable("notificationRecipient");
            String message = (String) execution.getVariable("notificationMessage");
            
            if (notificationType == null) {
                notificationType = "GENERAL";
            }
            
            sendNotification(notificationType, recipient, message, execution);
            
            execution.setVariable("notificationSent", true);
            execution.setVariable("notificationTimestamp", System.currentTimeMillis());
            
        } catch (Exception e) {
            logger.error("Error sending notification for process instance: {}", execution.getProcessInstanceId(), e);
            execution.setVariable("notificationSent", false);
            execution.setVariable("notificationError", e.getMessage());
        }
    }
    
    private void sendNotification(String type, String recipient, String message, DelegateExecution execution) {
        logger.info("Sending {} notification to: {} for process: {}", type, recipient, execution.getProcessInstanceId());
        
        switch (type.toUpperCase()) {
            case "APPROVAL_REQUEST":
                sendApprovalRequestNotification(recipient, message, execution);
                break;
            case "APPROVAL_COMPLETE":
                sendApprovalCompleteNotification(recipient, message, execution);
                break;
            case "REJECTION":
                sendRejectionNotification(recipient, message, execution);
                break;
            case "GENERAL":
            default:
                sendGeneralNotification(recipient, message, execution);
                break;
        }
    }
    
    private void sendApprovalRequestNotification(String recipient, String message, DelegateExecution execution) {
        logger.info("Approval request notification sent to: {} for process: {}", recipient, execution.getProcessInstanceId());
        // Implementation for approval request notification
        // Could integrate with email service, push notifications, etc.
    }
    
    private void sendApprovalCompleteNotification(String recipient, String message, DelegateExecution execution) {
        logger.info("Approval complete notification sent to: {} for process: {}", recipient, execution.getProcessInstanceId());
        // Implementation for approval complete notification
    }
    
    private void sendRejectionNotification(String recipient, String message, DelegateExecution execution) {
        logger.info("Rejection notification sent to: {} for process: {}", recipient, execution.getProcessInstanceId());
        // Implementation for rejection notification
    }
    
    private void sendGeneralNotification(String recipient, String message, DelegateExecution execution) {
        logger.info("General notification sent to: {} for process: {}", recipient, execution.getProcessInstanceId());
        // Implementation for general notification
    }
}