/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.yourorg.fineract.plugins.overdraftproduct.serialization;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OverdraftProductCommand {

    @NotNull
    private Long loanProductId;

    @NotNull
    @Size(min = 1, max = 200)
    private String name;

    @Size(max = 500)
    private String description;

    private String externalId;

    @PositiveOrZero
    private BigDecimal minimumLimit;

    @PositiveOrZero
    private BigDecimal maximumLimit;

    @PositiveOrZero
    private BigDecimal defaultLimit;

    @NotNull
    private LocalDate effectiveFrom;

    private LocalDate effectiveUntil;

    private BigDecimal interestRate;

    private Integer interestCalculationPeriodType;

    private String postingStrategy;

    private String roundingMode;

    private String versionLabel;

    @Valid
    private List<OverdraftFeeCommand> fees = Collections.emptyList();

    @Valid
    private List<OverdraftUsageRuleCommand> usageRules = Collections.emptyList();

    @Valid
    private OverdraftAccountingCommand accounting;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverdraftFeeCommand {

        @NotNull
        private String feeType;

        @PositiveOrZero
        private BigDecimal flatAmount;

        @PositiveOrZero
        private BigDecimal percentage;

        private String currencyCode;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverdraftUsageRuleCommand {

        @NotNull
        private String channel;

        @PositiveOrZero
        private BigDecimal perTransactionLimit;

        @PositiveOrZero
        private BigDecimal dailyLimit;

        @PositiveOrZero
        private BigDecimal monthlyLimit;

        private boolean whitelist = true;

        private Set<String> merchantCategoryCodes;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OverdraftAccountingCommand {

        private Long loanPortfolioAccountId;
        private Long interestIncomeAccountId;
        private Long feeIncomeAccountId;
        private Long receivableInterestAccountId;
        private Long receivableFeeAccountId;
        private Long chargeOffExpenseAccountId;
    }
}
