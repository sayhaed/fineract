package org.apache.fineract.portfolio.overdraftproduct.repository;

import java.util.List;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftUsageRule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OverdraftUsageRuleRepository extends JpaRepository<OverdraftUsageRule, Long> {

    List<OverdraftUsageRule> findByProductId(Long productId);

    List<OverdraftUsageRule> findByProductIdAndChannelIgnoreCase(Long productId, String channel);
}
