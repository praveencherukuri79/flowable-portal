package com.example.backend.controller;

import com.example.backend.model.RetentionOffer;
import com.example.backend.service.RetentionOfferService;
import com.example.backend.util.ResponseUtils;
import com.example.backend.util.ValidationUtils;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/offers")
public class RetentionOfferController {
    @Autowired
    private RetentionOfferService service;

    @Operation(summary = "Create a retention offer")
    @PostMapping
    public ResponseEntity<Map<String, Object>> createOffer(@RequestBody RetentionOffer offer) {
        try {
            // Validate input
            ValidationUtils.requireNonNull(offer, "Offer cannot be null");
            ValidationUtils.requireNonEmpty(offer.getUsername(), "Username is required");
            
            RetentionOffer createdOffer = service.createOffer(offer);
            return ResponseEntity.ok(ResponseUtils.successResponse("Retention offer created successfully", createdOffer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseUtils.errorResponse("Validation failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseUtils.errorResponse("Failed to create offer", e.getMessage()));
        }
    }

    @Operation(summary = "Get a retention offer by ID")
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getOffer(@PathVariable Long id) {
        try {
            ValidationUtils.requireNonNull(id, "ID cannot be null");
            ValidationUtils.requirePositive(id, "ID must be positive");
            
            Optional<RetentionOffer> offer = service.getOffer(id);
            if (offer.isPresent()) {
                return ResponseEntity.ok(ResponseUtils.successResponse("Offer found", offer.get()));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseUtils.errorResponse("Validation failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseUtils.errorResponse("Failed to get offer", e.getMessage()));
        }
    }

    @Operation(summary = "Get all retention offers")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllOffers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        try {
            ValidationUtils.requireNonNegative(page, "Page number cannot be negative");
            ValidationUtils.requirePositive(size, "Page size must be positive");
            
            List<RetentionOffer> offers = service.getAllOffers();
            
            // Simple pagination (in real app, use Spring Data pagination)
            int start = page * size;
            int end = Math.min(start + size, offers.size());
            List<RetentionOffer> paginatedOffers = offers.subList(start, end);
            
            return ResponseEntity.ok(ResponseUtils.successWithPagination(
                "Offers retrieved successfully",
                paginatedOffers,
                page,
                size,
                offers.size()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseUtils.errorResponse("Failed to get offers", e.getMessage()));
        }
    }

    @Operation(summary = "Update a retention offer")
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateOffer(@PathVariable Long id, @RequestBody RetentionOffer offer) {
        try {
            ValidationUtils.requireNonNull(id, "ID cannot be null");
            ValidationUtils.requirePositive(id, "ID must be positive");
            ValidationUtils.requireNonNull(offer, "Offer cannot be null");
            
            RetentionOffer updatedOffer = service.updateOffer(id, offer);
            return ResponseEntity.ok(ResponseUtils.successResponse("Offer updated successfully", updatedOffer));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseUtils.errorResponse("Validation failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseUtils.errorResponse("Failed to update offer", e.getMessage()));
        }
    }

    @Operation(summary = "Delete a retention offer")
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteOffer(@PathVariable Long id) {
        try {
            ValidationUtils.requireNonNull(id, "ID cannot be null");
            ValidationUtils.requirePositive(id, "ID must be positive");
            
            service.deleteOffer(id);
            return ResponseEntity.ok(ResponseUtils.successResponse("Offer deleted successfully", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ResponseUtils.errorResponse("Validation failed", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(ResponseUtils.errorResponse("Failed to delete offer", e.getMessage()));
        }
    }
}
