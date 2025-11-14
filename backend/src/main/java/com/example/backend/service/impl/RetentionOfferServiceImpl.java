package com.example.backend.service.impl;

import com.example.backend.model.RetentionOffer;
import com.example.backend.repository.RetentionOfferRepository;
import com.example.backend.service.RetentionOfferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RetentionOfferServiceImpl implements RetentionOfferService {
    @Autowired
    private RetentionOfferRepository repository;

    @Override
    @CacheEvict(value = "offers", allEntries = true)
    public RetentionOffer createOffer(RetentionOffer offer) {
        return repository.save(offer);
    }

    @Override
    @Cacheable(value = "offers", key = "#id")
    public Optional<RetentionOffer> getOffer(Long id) {
        return repository.findById(id);
    }

    @Override
    @Cacheable(value = "offers")
    public List<RetentionOffer> getAllOffers() {
        return repository.findAll();
    }

    @Override
    @CacheEvict(value = "offers", key = "#id")
    public RetentionOffer updateOffer(Long id, RetentionOffer offer) {
        offer.setId(id);
        return repository.save(offer);
    }

    @Override
    @CacheEvict(value = "offers", key = "#id")
    public void deleteOffer(Long id) {
        repository.deleteById(id);
    }
}