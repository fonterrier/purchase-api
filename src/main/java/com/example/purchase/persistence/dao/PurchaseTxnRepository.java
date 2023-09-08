package com.example.purchase.persistence.dao;

import com.example.purchase.persistence.model.PurchaseTxn;
import java.util.UUID;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * I am normally used to using Ebean ORM at my job.
 * <p>
 * For purposes of this coding test, using Hibernate/JPA with H2DB in-memory database, as solution should be runnable
 * without installing additional software stack components. Ebean test requires Docker.
 */
@Repository
public interface PurchaseTxnRepository extends CrudRepository<PurchaseTxn, UUID> {

}
