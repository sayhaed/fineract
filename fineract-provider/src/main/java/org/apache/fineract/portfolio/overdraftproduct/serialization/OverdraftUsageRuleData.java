package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.math.BigDecimal;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OverdraftUsageRuleData {

    Long id;
    String channel;
    boolean allowed;
    Set<String> merchantCategoryWhitelist;
    Set<String> merchantCategoryBlacklist;
    BigDecimal transactionLimit;
    BigDecimal dailyLimit;
    BigDecimal monthlyLimit;
    Integer coolingPeriodMinutes;
}
