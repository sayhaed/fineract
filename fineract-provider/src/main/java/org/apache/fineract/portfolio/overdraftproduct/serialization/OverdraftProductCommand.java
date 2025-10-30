package org.apache.fineract.portfolio.overdraftproduct.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftDayCountBasis;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftInterestMethod;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftRoundingMode;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverdraftProductCommand {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String currencyCode;

    @NotNull
    private BigDecimal defaultLimit;

    @NotNull
    private BigDecimal minimumLimit;

    @NotNull
    private BigDecimal maximumLimit;

    @NotNull
    private BigDecimal interestRate;

    @NotNull
    private OverdraftInterestMethod interestMethod;

    @NotNull
    private OverdraftDayCountBasis dayCountBasis;

    private Integer roundingScale;

    private OverdraftRoundingMode roundingMode;

    private boolean compoundingEnabled;

    private String postingSchedule;

    private boolean allowAccountOverride;

    private Long loanProductId;

    @Builder.Default
    @Valid
    private List<OverdraftFeeCommand> fees = new ArrayList<>();

    @Builder.Default
    @Valid
    private List<OverdraftUsageRuleCommand> usageRules = new ArrayList<>();

    @Valid
    private OverdraftProductAccountingCommand accounting;

    @Builder.Default
    @Valid
    private List<OverdraftProductVersionCommand> versions = new ArrayList<>();
}
