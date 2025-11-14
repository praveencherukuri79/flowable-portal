package com.example.backend.service;

import com.example.backend.model.RetentionOffer;
import java.util.List;
import java.util.Optional;

public interface RetentionOfferService {
    RetentionOffer createOffer(RetentionOffer offer);
    Optional<RetentionOffer> getOffer(Long id);
    List<RetentionOffer> getAllOffers();
    RetentionOffer updateOffer(Long id, RetentionOffer offer);
    void deleteOffer(Long id);
}
