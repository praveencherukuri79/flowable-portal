package com.example.backend.util;

import java.util.Set;

/**
 * Constants for process-start rules keyed by process definition key.
 */
public final class FlowableProcessStartRuleConstants {

    public static final String THREE_STAGE_PROCESS = "threeStageProcess";

    public static final Set<String> SINGLE_INSTANCE_PROCESS_KEYS = Set.of(
            THREE_STAGE_PROCESS
    );

    private FlowableProcessStartRuleConstants() {
    }
}