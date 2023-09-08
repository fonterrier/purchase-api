package com.example.purchase.persistence.model;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Entity
// not seeing any need for any extra indexes or unique constraints besides the primary index and key
@Table(name = "purchase_txn")
public class PurchaseTxn {

    @Id
    @GeneratedValue
    @Column(length = 16)
    private UUID id;

    @Column(length = 50, nullable = false)
    private String description;
    @Column(nullable = false)
    private Instant txnDate;
    // Non-negative purchase amount in USD rounded to the nearest cent
    @DecimalMin(value = "0.00", inclusive = true)
    @Digits(integer = 19, fraction = 2)
    @Column(nullable = false)
    private BigDecimal amount;

}
