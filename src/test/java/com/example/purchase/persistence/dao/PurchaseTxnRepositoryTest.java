package com.example.purchase.persistence.dao;

import com.example.purchase.persistence.model.PurchaseTxn;
import com.example.purchase.support.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PurchaseTxnRepositoryTest {
    @Autowired
    private PurchaseTxnRepository repository;

    // Hibernate only validates when flush() is called (entity actually persisted)
    @Autowired
    private EntityManager entityManager;

    private PurchaseTxn purchaseTxn;

    @BeforeEach
    void setUp() {
        purchaseTxn = TestHelper.createValidPurchaseTxn();
        assertEquals(0L, repository.count());
    }

    // happy path test
    @Test
    void saveAndGetPurchaseTxn_success() {
        savePurchaseTxn();
        assertNotNull(purchaseTxn.getId());
        assertEquals(1L, repository.count());

        PurchaseTxn retrieved = repository.findById(purchaseTxn.getId()).get();
        assertEquals(purchaseTxn, retrieved);
    }

    @Test
    void savePurchaseTxn_missingFields_TxnDate_Fail() {
        purchaseTxn.setTxnDate(null);
        assertThrows(PersistenceException.class, this::savePurchaseTxn);
    }

    @Test
    void savePurchaseTxn_missingFields_Description_Fail() {
        purchaseTxn.setDescription(null);
        assertThrows(PersistenceException.class, this::savePurchaseTxn);
    }

    @Test
    void savePurchaseTxn_missingFields_Amount_Fail() {
        purchaseTxn.setAmount(null);
        assertThrows(PersistenceException.class, this::savePurchaseTxn);
    }

    @Test
    void savePurchaseTxn_constraint_Description_Fail() {
        purchaseTxn.setDescription("Verylonglonglonglongerthan50characterssolonglonglonglonglonglonglonglonglonglong");
        assertThrows(PersistenceException.class, this::savePurchaseTxn);
    }

    @Test
    void savePurchaseTxn_constraint_Amount_Fail() {
        purchaseTxn.setAmount(new BigDecimal("50.015"));
        assertThrows(ConstraintViolationException.class, this::savePurchaseTxn);
    }

    private void savePurchaseTxn() {
        repository.save(purchaseTxn);
        entityManager.flush();
    }

}