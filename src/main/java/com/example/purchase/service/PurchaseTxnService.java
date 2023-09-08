package com.example.purchase.service;

import com.example.purchase.api.model.PurchaseTxnCurrencyDto;
import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.mapper.PurchaseTxnMapper;
import com.example.purchase.persistence.dao.PurchaseTxnRepository;
import com.example.purchase.persistence.model.PurchaseTxn;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Business logic layer
 */
@RequiredArgsConstructor
@Slf4j
@Service
public class PurchaseTxnService {
    private final PurchaseTxnRepository repository;
    private final CurrencyConversionService currencyConversionService;

    public PurchaseTxnDto createPurchaseTxn(@Nonnull final PurchaseTxnDto dto) {
        // fields already validated
        PurchaseTxn data = repository.save(PurchaseTxnMapper.INSTANCE.dtoToPurchaseTxn(dto));

        return PurchaseTxnMapper.INSTANCE.purchaseTxnToDto(data);
    }

    public PurchaseTxnCurrencyDto getPurchaseTxnCurrency(@Nonnull final UUID uuid, @Nonnull final String countryCurrencyDesc) throws HttpClientErrorException, IOException {
        // fields already validated
        Optional<PurchaseTxn> optData = repository.findById(uuid);
        // not too sure about the choice of Exception thrown - want abstraction from HTTP layer
        if (optData.isEmpty()) throw new HttpClientErrorException(HttpStatus.NOT_FOUND, "PurchaseTxn with id=" + uuid + " not found in database");

        PurchaseTxn data = optData.get();

        // retrieve exchange rate from within the last 6 months
        OffsetDateTime txnDate = PurchaseTxnMapper.INSTANCE.fromInstant(data.getTxnDate());
        Optional<BigDecimal> optExchangeRate = currencyConversionService.getExchangeRate(countryCurrencyDesc,
                txnDate.minusMonths(6L),
                txnDate);
        if (optExchangeRate.isEmpty()) throw new HttpClientErrorException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Missing " + currencyConversionService + " exchange rate for " + txnDate);

        BigDecimal exchangeRate = optExchangeRate.get();
        // rounded to two decimal places (i.e., cent)
        BigDecimal amountConverted = data.getAmount().multiply(exchangeRate).setScale(2, RoundingMode.HALF_UP);

        PurchaseTxnCurrencyDto dto = new PurchaseTxnCurrencyDto();
        dto.setId(data.getId());
        dto.setDescription(data.getDescription());
        dto.setTxnDate(txnDate);
        dto.setAmount(data.getAmount());
        dto.setCountryCurrencyDesc(countryCurrencyDesc);
        dto.setExchangeRate(exchangeRate);
        dto.setAmountConverted(amountConverted);

        return dto;
    }

}
