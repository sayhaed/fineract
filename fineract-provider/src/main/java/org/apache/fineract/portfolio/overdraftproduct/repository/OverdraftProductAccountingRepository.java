package org.apache.fineract.portfolio.overdraftproduct.repository;

import java.util.Optional;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductAccounting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdraftProductAccountingRepository extends JpaRepository<OverdraftProductAccounting, Long> {

    Optional<OverdraftProductAccounting> findByProductId(Long productId);
}
