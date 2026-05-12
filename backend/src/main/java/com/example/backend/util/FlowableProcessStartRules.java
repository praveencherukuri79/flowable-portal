package com.example.backend.util;

/**
 * Hardcoded start-time rules keyed by process definition key.
 */
public final class FlowableProcessStartRules {

    private FlowableProcessStartRules() {
    }

    public static boolean allowsMultipleInstances(String processDefinitionKey) {
        return !FlowableProcessStartRuleConstants.SINGLE_INSTANCE_PROCESS_KEYS.contains(processDefinitionKey);
    }
}