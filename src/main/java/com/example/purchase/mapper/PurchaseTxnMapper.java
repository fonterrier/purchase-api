package com.example.purchase.mapper;

import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.persistence.model.PurchaseTxn;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

// could use org.mapstruct @Mapper to autogenerate mapper and reduce boilerplate
public class PurchaseTxnMapper {

    public static final PurchaseTxnMapper INSTANCE = new PurchaseTxnMapper();

    public PurchaseTxnDto purchaseTxnToDto(PurchaseTxn data) {
        if (data == null) {
            return null;
        }

        var dto = new PurchaseTxnDto();

        dto.setDescription(data.getDescription());
        dto.setTxnDate(fromInstant(data.getTxnDate()));
        dto.setAmount(data.getAmount());

        return dto;
    }

    public PurchaseTxn dtoToPurchaseTxn(PurchaseTxnDto dto) {
        if (dto == null) {
            return null;
        }

        var data = new PurchaseTxn();

        data.setDescription(dto.getDescription());
        data.setTxnDate(toInstant(dto.getTxnDate()));
        data.setAmount(dto.getAmount());

        return data;
    }

    public Instant toInstant(OffsetDateTime value) {
        return value == null ? null : value.atZoneSameInstant(ZoneId.of("UTC")).toInstant();
    }

    public OffsetDateTime fromInstant(Instant value) {
        return value == null ? null : value.atOffset(ZoneOffset.UTC);
    }


}
