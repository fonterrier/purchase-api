package com.example.purchase.service;

import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.mapper.PurchaseTxnMapper;
import com.example.purchase.persistence.dao.PurchaseTxnRepository;
import com.example.purchase.persistence.model.PurchaseTxn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;

/**
 * Business logic layer
 */
@RequiredArgsConstructor
@Slf4j
@Component
public class PurchaseTxnService {
    private final PurchaseTxnRepository repository;

    public PurchaseTxnDto createPurchaseTxn(@Nonnull final PurchaseTxnDto dto) {
        // fields already validated
        PurchaseTxn data = repository.save(PurchaseTxnMapper.INSTANCE.dtoToPurchaseTxn(dto));

        PurchaseTxnDto createdDto = PurchaseTxnMapper.INSTANCE.purchaseTxnToDto(data);
        createdDto.setId(data.getId());
        return createdDto;
    }

}
