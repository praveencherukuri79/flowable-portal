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
@Schema(description = "Paginated response wrapper")
public class PagedResponse<T> {
    @Schema(description = "List of items in current page")
    public List<T> content;
    
    @Schema(description = "Total number of items")
    public long total;
}

