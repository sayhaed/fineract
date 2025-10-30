package org.apache.fineract.portfolio.overdraftproduct.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverdraftProductAccountingCommand {

    @NotNull
    private Long receivableAccountId;

    @NotNull
    private Long interestIncomeAccountId;

    private Long feeIncomeAccountId;

    private Long suspenseAccountId;

    private Long writeOffAccountId;

    private Long penaltyIncomeAccountId;

    private Boolean accrualEnabled;
}
