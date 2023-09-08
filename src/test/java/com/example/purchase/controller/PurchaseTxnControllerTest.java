package com.example.purchase.controller;

import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.service.PurchaseTxnService;
import com.example.purchase.support.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// TODO: add integration tests in another class

/**
 * Test controller API input validation and response codes
 */
@WebMvcTest
class PurchaseTxnControllerTest {
    private static final String API_PATH = "/purchase-txn";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private PurchaseTxnService purchaseTxnService;

    @BeforeEach
    void setUp() {
        given(purchaseTxnService.createPurchaseTxn(any())).willReturn(new PurchaseTxnDto());
    }

    // happy path test
    @Test
    void postPurchaseTxn() throws Exception {
        this.mockMvc.perform(
                        post(API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(TestHelper.createValidPurchaseTxnDto()))
                )
                .andExpect(status().is(201));

        verify(purchaseTxnService, times(1)).createPurchaseTxn(any());
    }

    /**
     * Advantage of using OpenAPI generator is it automatically generates code to validate required
     * parameters, parameter formats and restrictions (e.g. length)
     */
    @Test
    void postPurchaseTxn_missingParameters_isBadRequest() throws Exception {
        this.mockMvc.perform(
                        post(API_PATH)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().is(400));
    }

    // check all validations (field length, cents rounding)
    @Test
    void postPurchaseTxn2_invalidParameters_isBadRequest() throws Exception {
        PurchaseTxnDto dto = TestHelper.createValidPurchaseTxnDto();
        dto.setDescription("Verylonglonglonglongerthan50characterssolonglonglonglonglonglonglonglonglonglong");
        assertEquals(400, performPostPurchaseTxn(dto).getStatus());

        // non-negative and 2 decimal places required
        dto = TestHelper.createValidPurchaseTxnDto();
        dto.setAmount(new BigDecimal("0.00"));
        assertEquals(201, performPostPurchaseTxn(dto).getStatus());
        dto.setAmount(new BigDecimal("1379.95"));
        assertEquals(201, performPostPurchaseTxn(dto).getStatus());
        dto.setAmount(new BigDecimal("1379"));
        assertEquals(201, performPostPurchaseTxn(dto).getStatus());
        dto.setAmount(new BigDecimal("-5.00"));
        assertEquals(400, performPostPurchaseTxn(dto).getStatus());
        dto.setAmount(new BigDecimal("7.034"));
        assertEquals(400, performPostPurchaseTxn(dto).getStatus());
    }

    private MockHttpServletResponse performPostPurchaseTxn(PurchaseTxnDto content) throws Exception {
        return this.mockMvc.perform(
                post(API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(content))).andReturn().getResponse();
    }

    // happy path test
    @Test
    void getPurchaseTxn() throws Exception {
        this.mockMvc.perform(
                        get(API_PATH + "/" + UUID.randomUUID())
                                .queryParam("countryCurrencyDesc", "Mexico-Peso")
                )
                .andExpect(status().is(200));
    }

    @Test
    void getPurchaseTxn_missingParameters_isBadRequest() throws Exception {
        this.mockMvc.perform(get(API_PATH + "/" + UUID.randomUUID())).andExpect(status().is(400));
    }

}