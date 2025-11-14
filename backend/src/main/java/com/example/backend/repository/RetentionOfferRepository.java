package com.example.backend.repository;

import com.example.backend.model.RetentionOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RetentionOfferRepository extends JpaRepository<RetentionOffer, Long> {
}
