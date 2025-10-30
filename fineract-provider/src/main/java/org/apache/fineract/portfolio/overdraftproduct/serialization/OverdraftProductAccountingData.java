package org.apache.fineract.portfolio.overdraftproduct.serialization;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class OverdraftProductAccountingData {

    Long receivableAccountId;
    Long interestIncomeAccountId;
    Long feeIncomeAccountId;
    Long suspenseAccountId;
    Long writeOffAccountId;
    Long penaltyIncomeAccountId;
    boolean accrualEnabled;
}
