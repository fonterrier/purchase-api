package com.example.purchase.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.purchase.api.model.ErrorDetailsDto;
import com.example.purchase.api.model.PurchaseTxnCurrencyDto;
import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.mapper.PurchaseTxnMapper;
import com.example.purchase.persistence.dao.PurchaseTxnRepository;
import com.example.purchase.persistence.model.PurchaseTxn;
import com.example.purchase.service.CurrencyConversionService;
import com.example.purchase.service.PurchaseTxnService;
import com.example.purchase.support.TestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test controller integration tests (using actual in-memory test DB) Run during mvn verify stage
 */
@SpringBootTest
@AutoConfigureMockMvc
// @AutoConfigureTestDatabase // use embedded database instead of real database
class PurchaseTxnControllerIntegrationTest {

  private static final String API_PATH = "/purchase-txn";

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private ObjectMapper objectMapper;
  @SpyBean
  private PurchaseTxnService purchaseTxnService;
  @SpyBean
  private PurchaseTxnRepository repository;
  @SpyBean
  private CurrencyConversionService currencyConversionService;

  // happy path test
  @Test
  void postAndGetPurchaseTxn() throws Exception {
    // POST test
    PurchaseTxnDto sentDto = TestHelper.createValidPurchaseTxnDto();

    MockHttpServletResponse response = this.mockMvc.perform(
            post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sentDto))
        )
        .andExpect(status().is(201))
        .andReturn().getResponse();

    verify(purchaseTxnService, times(1)).createPurchaseTxn(any());
    verify(repository, times(1)).save(any());
    assertEquals(1L, repository.count());
    PurchaseTxn stored = repository.findAll().iterator().next();
    PurchaseTxnDto recvDto = objectMapper.readValue(response.getContentAsString(), PurchaseTxnDto.class);

    assertNotNull(recvDto.getId());
    assertEquals(PurchaseTxnMapper.INSTANCE.purchaseTxnToDto(stored), recvDto);
    recvDto.setId(null);
    assertEquals(sentDto, recvDto);

    // GET test
    // integration test would not normally call external API, because response apt to change
    // also CI/CD pipeline may have network restrictions for security reasons
    doReturn(Optional.of(new BigDecimal("0.121111113")))
        .when(currencyConversionService).getExchangeRate(any(), any(), any());

    response = this.mockMvc.perform(
            get(API_PATH + "/" + stored.getId())
                .queryParam("countryCurrencyDesc", "Mexico-Peso")
        )
        .andExpect(status().is(200))
        .andReturn().getResponse();

    assertNotNull(objectMapper.readValue(response.getContentAsString(), PurchaseTxnCurrencyDto.class));
  }

  @Test
  void getPurchaseTxn_notFound() throws Exception {
    UUID nonExistentId = UUID.randomUUID();
    MockHttpServletResponse response = this.mockMvc.perform(
            get(API_PATH + "/" + nonExistentId)
                .queryParam("countryCurrencyDesc", "Mexico-Peso")
        )
        .andExpect(status().is(404))
        .andReturn().getResponse();

    ErrorDetailsDto errorDetails = objectMapper.readValue(response.getContentAsString(), ErrorDetailsDto.class);
    assertEquals(404, errorDetails.getCode());
    assertEquals("PurchaseTxn with id=" + nonExistentId + " not found in database", errorDetails.getMessage());
  }

}