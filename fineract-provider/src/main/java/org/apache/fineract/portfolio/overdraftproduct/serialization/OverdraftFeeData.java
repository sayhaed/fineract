package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Value;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeCalculationType;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeType;

@Value
@Builder
public class OverdraftFeeData {

    Long id;
    OverdraftFeeType feeType;
    OverdraftFeeCalculationType calculationType;
    BigDecimal amount;
    BigDecimal percentage;
    String tierDefinition;
    String triggerCondition;
    boolean active;
}
