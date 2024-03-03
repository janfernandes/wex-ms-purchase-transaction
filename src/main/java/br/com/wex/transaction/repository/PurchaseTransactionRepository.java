package br.com.wex.transaction.repository;

import br.com.wex.transaction.repository.entity.PurchaseTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Responsible for the database operations.
 */
@Repository
public interface PurchaseTransactionRepository extends JpaRepository<PurchaseTransactionEntity, Long> {

}
