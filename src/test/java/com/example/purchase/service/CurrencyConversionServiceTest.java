package com.example.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CurrencyConversionServiceTest {
    @Autowired
    private CurrencyConversionService service;

    @Test
    void constructExchangeRateUrl() {
        assertEquals(CurrencyConversionService.EXCHANGE_RATE_URL
                        + "?filter=country_currency_desc:in:Mexico-Peso,record_date:gte:2019-10-12,record_date:lte:2020-02-25",
                service.constructExchangeRateUrl("Mexico-Peso",
                        OffsetDateTime.parse("2019-10-12T07:20:50.52Z"),
                        OffsetDateTime.parse("2020-02-25T08:20:50.52Z")));
    }

    // happy path
    @Test
    void getExchangeRateFromResponseBody() throws JsonProcessingException {
        String responseBody = "{\"data\":[{\"exchange_rate\":\"19.913\",\"record_date\":\"2020-12-31\"},{\"exchange_rate\":\"20.067\",\"record_date\":\"2020-09-30\"},{\"exchange_rate\":\"23.164\",\"record_date\":\"2020-06-30\"},{\"exchange_rate\":\"23.791\",\"record_date\":\"2020-03-31\"}],\"meta\":{\"count\":4,\"labels\":{\"exchange_rate\":\"Exchange Rate\",\"record_date\":\"Record Date\"},\"dataTypes\":{\"exchange_rate\":\"NUMBER\",\"record_date\":\"DATE\"},\"dataFormats\":{\"exchange_rate\":\"10.2\",\"record_date\":\"YYYY-MM-DD\"},\"total-count\":4,\"total-pages\":1},\"links\":{\"self\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"first\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"prev\":null,\"next\":null,\"last\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\"}}";
        Optional<BigDecimal> exchangeRate = service.getExchangeRateFromResponseBody(responseBody);

        assertTrue(exchangeRate.isPresent());
        System.out.println("Exchange Rate: " + exchangeRate.get());
        assertEquals(0, exchangeRate.get().compareTo(new BigDecimal("19.913")));
    }

    // exchange rate not found
    @Test
    void getExchangeRateFromResponseBody_NotFound() throws JsonProcessingException {
        String responseBody = "{\"data\":[],\"meta\":{\"count\":0,\"labels\":{\"exchange_rate\":\"Exchange Rate\",\"record_date\":\"Record Date\"},\"dataTypes\":{\"exchange_rate\":\"NUMBER\",\"record_date\":\"DATE\"},\"dataFormats\":{\"exchange_rate\":\"10.2\",\"record_date\":\"YYYY-MM-DD\"},\"total-count\":0,\"total-pages\":0},\"links\":{\"self\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"first\":\"&page%5Bnumber%5D=1&page%5Bsize%5D=100\",\"prev\":null,\"next\":\"&page%5Bnumber%5D=2&page%5Bsize%5D=100\",\"last\":\"&page%5Bnumber%5D=0&page%5Bsize%5D=100\"}}";
        Optional<BigDecimal> exchangeRate = service.getExchangeRateFromResponseBody(responseBody);

        assertTrue(exchangeRate.isEmpty());
    }

    // unrecognised response
    @Test
    void getExchangeRateFromResponseBody_Unrecognised() {
        String responseBody = " {\"data\":[huh?]}";
        assertThrows(JsonProcessingException.class, () -> service.getExchangeRateFromResponseBody(responseBody));
    }
}