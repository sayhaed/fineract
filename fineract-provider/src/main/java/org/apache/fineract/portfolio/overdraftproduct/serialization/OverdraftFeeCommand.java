package org.apache.fineract.portfolio.overdraftproduct.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeCalculationType;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class OverdraftFeeCommand {

    @NotNull
    private OverdraftFeeType feeType;

    @NotNull
    private OverdraftFeeCalculationType calculationType;

    private BigDecimal amount;

    private BigDecimal percentage;

    private String tierDefinition;

    private String triggerCondition;

    private Boolean active;
}
