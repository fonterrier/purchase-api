package com.example.purchase.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.http.client.HttpResponseException;
import org.springframework.stereotype.Service;

/**
 * Service for calling the Treasury Reporting Rates of Exchange API to get currency conversion rates
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class CurrencyConversionService {

  protected static final String EXCHANGE_RATE_URL = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange";
  private static final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private final OkHttpClient httpClient;
  private final ObjectMapper objectMapper;

  /**
   * Get the Treasury API response for the country_currency_desc within the date range e.g. <a
   * href="https://api.fiscaldata.treasury.gov/services/api/fiscal_service/v1/accounting/od/rates_of_exchange?filter=country_currency_desc:in:Mexico-Peso,record_date:gte:2020-01-30,record_date:lte:2020-06-30">...</a>
   *
   * @param dateStart // TODO not clear from Treasure API whether date is in UTC or American timezones. Assume UTC for
   *                  now. At least the Americans are using the rational International yyyy-MM-dd formatting
   * @param dateEnd   // TODO not clear from Treasure API whether date is in UTC or American timezones. Assume UTC for
   *                  now. At least the Americans are using the rational International yyyy-MM-dd formatting
   * @return exchange rate. Empty if not found.
   */
  // TODO: Treasury API returns multiple rates. I wonder if we want to get the one closest to the PurchaseTxn date, instead of the first (like currently implemented).
  // It's not in the requirements, so leaving as first.
  public Optional<BigDecimal> getExchangeRate(@Nonnull String countryCurrencyDesc, @Nonnull OffsetDateTime dateStart,
      @Nonnull OffsetDateTime dateEnd) throws IOException {
    // TODO: in a real system we might do retries if failed, such as with a retry backoff strategy
    String url = constructExchangeRateUrl(countryCurrencyDesc, dateStart, dateEnd);
    log.info("Requesting Exchange Rate from Treasury API GET " + url);

    Request request = new Request.Builder()
        .url(url)
        .build();
    Call call = httpClient.newCall(request); // synchronous

    String responseBody;
    try (Response response = call.execute()) {
      // unsuccessful response
      if (response.code() != 200) {
        throw new HttpResponseException(response.code(), "Request not successful.");
      }

      // successful response:
      // e.g. {"data":[{"exchange_rate":"19.913","record_date":"2020-12-31"},{"exchange_rate":"20.067","record_date":"2020-09-30"},{"exchange_rate":"23.164","record_date":"2020-06-30"},{"exchange_rate":"23.791","record_date":"2020-03-31"}],"meta":{"count":4,"labels":{"exchange_rate":"Exchange Rate","record_date":"Record Date"},"dataTypes":{"exchange_rate":"NUMBER","record_date":"DATE"},"dataFormats":{"exchange_rate":"10.2","record_date":"YYYY-MM-DD"},"total-count":4,"total-pages":1},"links":{"self":"&page%5Bnumber%5D=1&page%5Bsize%5D=100","first":"&page%5Bnumber%5D=1&page%5Bsize%5D=100","prev":null,"next":null,"last":"&page%5Bnumber%5D=1&page%5Bsize%5D=100"}}
      // or, when exchange rate not found:
      // e.g. {"data":[],"meta":{"count":0,"labels":{"exchange_rate":"Exchange Rate","record_date":"Record Date"},"dataTypes":{"exchange_rate":"NUMBER","record_date":"DATE"},"dataFormats":{"exchange_rate":"10.2","record_date":"YYYY-MM-DD"},"total-count":0,"total-pages":0},"links":{"self":"&page%5Bnumber%5D=1&page%5Bsize%5D=100","first":"&page%5Bnumber%5D=1&page%5Bsize%5D=100","prev":null,"next":"&page%5Bnumber%5D=2&page%5Bsize%5D=100","last":"&page%5Bnumber%5D=0&page%5Bsize%5D=100"}}
      responseBody = response.body().string();
      log.debug("Received response body: " + responseBody);
    }

    Optional<BigDecimal> exchangeRate;
    try {
      exchangeRate = getExchangeRateFromResponseBody(responseBody);
    } catch (JsonProcessingException ex) {
      log.error("Unknown Treasury API response received");
      throw ex;
    }
    if (exchangeRate.isEmpty()) {
      log.info("Treasury API response could not find exchange rate");
    } else {
      log.info("Treasury API response found exchange rate {} for {}",
          exchangeRate.get(),
          countryCurrencyDesc);
    }
    return getExchangeRateFromResponseBody(responseBody);
  }

  protected Optional<BigDecimal> getExchangeRateFromResponseBody(String responseBody) throws JsonProcessingException {
    JsonNode data = objectMapper.readTree(responseBody).get("data");
    if (data.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new BigDecimal(data.get(0).get("exchange_rate").textValue()));
  }

  protected String constructExchangeRateUrl(@Nonnull String countryCurrencyDesc, @Nonnull OffsetDateTime dateStart,
      @Nonnull OffsetDateTime dateEnd) {
    return EXCHANGE_RATE_URL
        + "?filter=country_currency_desc:in:" + countryCurrencyDesc
        + ",record_date:gte:" + dtFormatter.format(dateStart)
        + ",record_date:lte:" + dtFormatter.format(dateEnd);
  }


}
