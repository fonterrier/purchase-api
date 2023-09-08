package com.example.purchase.service;

import com.example.purchase.api.model.PurchaseTxnCurrencyDto;
import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.persistence.dao.PurchaseTxnRepository;
import com.example.purchase.persistence.model.PurchaseTxn;
import com.example.purchase.support.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class PurchaseTxnServiceTest {
    private static final String COUNTRY_CURRENCY_DESC = "Mexico-Peso";

    private PurchaseTxnRepository repository;
    private PurchaseTxnService purchaseTxnService;
    private CurrencyConversionService currencyConversionService;

    private final UUID createdId = UUID.randomUUID();
    private final UUID existingId = UUID.randomUUID();
    private final UUID nonExistentId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        repository = mock(PurchaseTxnRepository.class);
        currencyConversionService = mock(CurrencyConversionService.class);
        purchaseTxnService = new PurchaseTxnService(repository, currencyConversionService);
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

    // happy path
    @Test
    void getPurchaseTxnCurrency() throws Exception {
        PurchaseTxn purchaseTxn = TestHelper.createValidPurchaseTxn();
        purchaseTxn.setId(existingId);

        when(repository.findById(any())).thenReturn(
                Optional.of(purchaseTxn)
        );
        when(currencyConversionService.getExchangeRate(any(), any(), any())).thenReturn(
                Optional.of(new BigDecimal("0.121111113"))
        );

        PurchaseTxnCurrencyDto dto = purchaseTxnService.getPurchaseTxnCurrency(purchaseTxn.getId(), COUNTRY_CURRENCY_DESC);
        verify(repository, times(1)).findById(purchaseTxn.getId());
        OffsetDateTime dateStart = OffsetDateTime.parse("2019-04-12T07:20:50.520Z");
        OffsetDateTime dateEnd = OffsetDateTime.parse("2019-10-12T07:20:50.520Z");
        verify(currencyConversionService, times(1)).getExchangeRate(COUNTRY_CURRENCY_DESC,
                dateStart, dateEnd);

        assertEquals(purchaseTxn.getId(), dto.getId());
        assertEquals(purchaseTxn.getDescription(), dto.getDescription());
        assertEquals(dateEnd, dto.getTxnDate());
        assertEquals("200.00", dto.getAmount().toString());
        assertEquals(COUNTRY_CURRENCY_DESC, dto.getCountryCurrencyDesc());
        assertEquals("0.121111113", dto.getExchangeRate().toString());
        assertEquals("24.22", dto.getAmountConverted().toString()); // verify to 2 decimal places
    }

    @Test
    void getPurchaseTxnCurrency_notFound() {
        PurchaseTxn purchaseTxn = TestHelper.createValidPurchaseTxn();
        purchaseTxn.setId(existingId);

        when(repository.findById(any())).thenReturn(
                Optional.empty()
        );

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> purchaseTxnService.getPurchaseTxnCurrency(purchaseTxn.getId(), COUNTRY_CURRENCY_DESC));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
    }

    @Test
    void getPurchaseTxnCurrency_unprocessable() throws Exception {
        PurchaseTxn purchaseTxn = TestHelper.createValidPurchaseTxn();
        purchaseTxn.setId(nonExistentId);

        when(repository.findById(any())).thenReturn(
                Optional.of(purchaseTxn)
        );
        when(currencyConversionService.getExchangeRate(any(), any(), any())).thenReturn(
                Optional.empty()
        );

        HttpClientErrorException ex = assertThrows(HttpClientErrorException.class,
                () -> purchaseTxnService.getPurchaseTxnCurrency(purchaseTxn.getId(), COUNTRY_CURRENCY_DESC));
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getStatusCode());
    }
}