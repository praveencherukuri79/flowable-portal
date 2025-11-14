package com.example.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Dashboard metrics for admin portal")
public class MetricsDto {
    @Schema(description = "Running process instances count")
    public long runningInstances;
    
    @Schema(description = "Completed process instances count")
    public long completedInstances;
    
    @Schema(description = "Total tasks count")
    public long totalTasks;
    
    @Schema(description = "Process instances by day (last 7 days)")
    public List<DailyCount> instancesByDay;
    
    @Schema(description = "Tasks grouped by state")
    public List<StateCount> tasksByState;
    
    @Schema(description = "Average duration by process definition")
    public List<DurationMetric> avgDurationByDefinition;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DailyCount {
        public String day;
        public long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StateCount {
        public String state;
        public long count;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DurationMetric {
        public String definitionKey;
        public double minutes;
    }
}

