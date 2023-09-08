package com.example.purchase.support;

import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.mapper.PurchaseTxnMapper;
import com.example.purchase.persistence.model.PurchaseTxn;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TestHelper {
    public static PurchaseTxnDto createValidPurchaseTxnDto() {
        PurchaseTxnDto dto = new PurchaseTxnDto();

        dto.setDescription("Airline ticket from New Zealand to Australia");
        dto.setTxnDate(OffsetDateTime.parse("2019-10-12T07:20:50.52Z"));
        dto.setAmount(new BigDecimal("200.00"));

        return dto;
    }

    public static PurchaseTxn createValidPurchaseTxn() {
        return PurchaseTxnMapper.INSTANCE.dtoToPurchaseTxn(createValidPurchaseTxnDto());
    }
}
