package com.example.purchase.service;

import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.persistence.dao.PurchaseTxnRepository;
import com.example.purchase.persistence.model.PurchaseTxn;
import com.example.purchase.support.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PurchaseTxnServiceTest {
    private PurchaseTxnRepository repository;
    private PurchaseTxnService purchaseTxnService;

    private final UUID createdId = UUID.randomUUID();
    private final UUID existingId = UUID.randomUUID();
    private final UUID nonExistentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(PurchaseTxnRepository.class);
        purchaseTxnService = new PurchaseTxnService(repository);
    }

    // happy path
    @Test
    void createPurchaseTxn() {
        PurchaseTxn data = TestHelper.createValidPurchaseTxn();
        data.setId(createdId);
        when(repository.save(any())).thenReturn(data);

        PurchaseTxnDto dto = purchaseTxnService.createPurchaseTxn(TestHelper.createValidPurchaseTxnDto());
        assertEquals(createdId, dto.getId());
    }
}