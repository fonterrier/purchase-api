package com.example.purchase.controller;

import com.example.purchase.api.api.PurchaseTxnApiDelegate;
import com.example.purchase.api.model.ErrorDetailsDto;
import com.example.purchase.api.model.PurchaseTxnDto;
import com.example.purchase.api.model.PurchaseTxnCurrencyDto;
import com.example.purchase.service.PurchaseTxnService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Controller
public class PurchaseTxnController implements PurchaseTxnApiDelegate {
    private final PurchaseTxnService purchaseTxnService;

    @Override
    public ResponseEntity postPurchaseTxn(PurchaseTxnDto dto) {
        log.info("Received postPurchaseTxn request");

        Optional<ErrorDetailsDto> errorDetails = this.isPurchaseTxnDtoValid(dto);
        if (errorDetails.isPresent()) {
            return ResponseEntity.badRequest()
                    .body(errorDetails.get());
        }

        PurchaseTxnDto created = purchaseTxnService.createPurchaseTxn(dto);

        return ResponseEntity.created(URI.create("/purchase-txn" + created.getId()))
                .body(created);
    }

    // TODO: dummy implementations for now
    @Override
    public ResponseEntity<PurchaseTxnCurrencyDto> getPurchaseTxn(UUID id, String countryCurrencyDesc) {
        log.info("Received getPurchaseTxn request");

        PurchaseTxnCurrencyDto dto = new PurchaseTxnCurrencyDto();

        return ResponseEntity.ok(dto);
    }

    /**
     * Validation for field format requirements that cannot be expressed via the OpenAPI spec and code
     * autogeneration
     *
     * @return empty optional if valid, otherwise Error response with the error reasons.
     */
    private Optional<ErrorDetailsDto> isPurchaseTxnDtoValid(PurchaseTxnDto dto) {
        List<String> errors = new ArrayList<>();

        // pattern: ^[0-9]*\.[0-9][0-9]$
        // Non-negative purchase amount in USD rounded to the nearest cent.
        if (dto.getAmount().compareTo(BigDecimal.ZERO) < 0) {
            errors.add("Amount must be non-negative");
        }
        if (dto.getAmount().scale() > 2) {
            errors.add("Amount must be specified to up to two decimal places");
        }

        if (errors.isEmpty()) return Optional.empty();

        ErrorDetailsDto errorDetails = new ErrorDetailsDto().code(400)
                .message(errors.toString());

        return Optional.of(errorDetails);
    }


}
