package org.apache.fineract.portfolio.overdraftproduct.repository;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OverdraftProductRepository
        extends JpaRepository<OverdraftProduct, Long>, JpaSpecificationExecutor<OverdraftProduct> {

    List<OverdraftProduct> findByStatus(OverdraftProductStatus status);

    List<OverdraftProduct> findByStatusAndCurrencyCode(OverdraftProductStatus status, String currencyCode);

    Optional<OverdraftProduct> findByLoanProductIdAndStatus(Long loanProductId, OverdraftProductStatus status);

    List<OverdraftProduct> findByCurrencyCodeIgnoreCase(String currencyCode);

    boolean existsByNameIgnoreCase(String name);
}
