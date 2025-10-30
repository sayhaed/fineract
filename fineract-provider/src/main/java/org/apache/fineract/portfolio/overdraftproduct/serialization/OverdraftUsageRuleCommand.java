package org.apache.fineract.portfolio.overdraftproduct.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Set;
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
public class OverdraftUsageRuleCommand {

    @NotBlank
    private String channel;

    @NotNull
    private Boolean allowed;

    private Set<String> merchantCategoryWhitelist;

    private Set<String> merchantCategoryBlacklist;

    private BigDecimal transactionLimit;

    private BigDecimal dailyLimit;

    private BigDecimal monthlyLimit;

    private Integer coolingPeriodMinutes;
}
