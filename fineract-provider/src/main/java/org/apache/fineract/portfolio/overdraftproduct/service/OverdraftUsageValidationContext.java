package org.apache.fineract.portfolio.overdraftproduct.service;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OverdraftUsageValidationContext {

    String channel;
    String merchantCategoryCode;
    BigDecimal transactionAmount;
    BigDecimal dayToDateUsage;
    BigDecimal monthToDateUsage;
}
