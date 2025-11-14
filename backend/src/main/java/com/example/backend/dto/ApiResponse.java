package com.example.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Generic API response wrapper")
public class ApiResponse {
    
    @Schema(description = "Response message", example = "Operation completed successfully")
    private String message;
    
    @Schema(description = "Response status", example = "SUCCESS")
    private String status;
    
    @Schema(description = "Additional data")
    private Object data;
    
    public static ApiResponse success(String message) {
        return ApiResponse.builder()
                .status("SUCCESS")
                .message(message)
                .build();
    }
    
    public static ApiResponse success(String message, Object data) {
        return ApiResponse.builder()
                .status("SUCCESS")
                .message(message)
                .data(data)
                .build();
    }
    
    public static ApiResponse error(String message) {
        return ApiResponse.builder()
                .status("ERROR")
                .message(message)
                .build();
    }
}

