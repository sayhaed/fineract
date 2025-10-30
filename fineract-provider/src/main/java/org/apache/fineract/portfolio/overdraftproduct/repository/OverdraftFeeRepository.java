package org.apache.fineract.portfolio.overdraftproduct.repository;

import java.util.List;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFee;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdraftFeeRepository extends JpaRepository<OverdraftFee, Long> {

    List<OverdraftFee> findByProductId(Long productId);

    List<OverdraftFee> findByProductIdAndFeeType(Long productId, OverdraftFeeType feeType);
}
