package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftDayCountBasis;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftInterestMethod;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductStatus;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftRoundingMode;

@Value
@Builder
public class OverdraftProductData {

    Long id;
    Long loanProductId;
    String name;
    String description;
    String currencyCode;
    BigDecimal defaultLimit;
    BigDecimal minimumLimit;
    BigDecimal maximumLimit;
    BigDecimal interestRate;
    OverdraftInterestMethod interestMethod;
    OverdraftDayCountBasis dayCountBasis;
    Integer roundingScale;
    OverdraftRoundingMode roundingMode;
    boolean compoundingEnabled;
    String postingSchedule;
    boolean allowAccountOverride;
    OverdraftProductStatus status;
    LocalDate activatedOn;
    LocalDate retiredOn;
    List<OverdraftFeeData> fees;
    List<OverdraftUsageRuleData> usageRules;
    OverdraftProductAccountingData accounting;
    List<OverdraftProductVersionData> versions;
}
