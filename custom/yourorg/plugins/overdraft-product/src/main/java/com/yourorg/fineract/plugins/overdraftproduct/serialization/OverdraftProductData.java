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

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftFee;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductAccounting;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductVersion;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftUsageRule;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Value;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;

@Value
@Builder
public class OverdraftProductData {

    Long id;
    Long loanProductId;
    String externalId;
    String name;
    String description;
    BigDecimal minimumLimit;
    BigDecimal maximumLimit;
    BigDecimal defaultLimit;
    boolean active;
    LocalDate validFrom;
    LocalDate validUntil;
    List<OverdraftProductVersionData> versions;

    public static OverdraftProductData from(OverdraftProduct product) {
        return OverdraftProductData.builder().id(product.getId()).loanProductId(product.getLoanProduct().getId())
                .externalId(product.getExternalId()).name(product.getName()).description(product.getDescription())
                .minimumLimit(product.getMinimumLimit()).maximumLimit(product.getMaximumLimit())
                .defaultLimit(product.getDefaultLimit()).active(product.isActive()).validFrom(product.getValidFrom())
                .validUntil(product.getValidUntil())
                .versions(product.getVersions().stream().map(OverdraftProductVersionData::from).collect(Collectors.toList()))
                .build();
    }

    @Value
    @Builder
    public static class OverdraftProductVersionData {

        Long id;
        String label;
        LocalDate effectiveFrom;
        LocalDate effectiveUntil;
        BigDecimal defaultLimit;
        BigDecimal interestRate;
        Integer interestCalculationPeriodType;
        String postingStrategy;
        String roundingMode;
        List<OverdraftFeeData> fees;
        List<OverdraftUsageRuleData> usageRules;
        OverdraftAccountingData accounting;

        public static OverdraftProductVersionData from(OverdraftProductVersion version) {
            return OverdraftProductVersionData.builder().id(version.getId()).label(version.getLabel())
                    .effectiveFrom(version.getEffectiveFrom()).effectiveUntil(version.getEffectiveUntil())
                    .defaultLimit(version.getDefaultLimit()).interestRate(version.getInterestRate())
                    .interestCalculationPeriodType(version.getInterestCalculationPeriodType())
                    .postingStrategy(version.getPostingStrategy()).roundingMode(version.getRoundingMode())
                    .fees(version.getFees().stream().map(OverdraftFeeData::from).collect(Collectors.toList()))
                    .usageRules(version.getUsageRules().stream().map(OverdraftUsageRuleData::from).collect(Collectors.toList()))
                    .accounting(OverdraftAccountingData.from(version.getAccounting())).build();
        }
    }

    @Value
    @Builder
    public static class OverdraftFeeData {

        Long id;
        String feeType;
        BigDecimal flatAmount;
        BigDecimal percentage;
        String currencyCode;

        public static OverdraftFeeData from(OverdraftFee fee) {
            return OverdraftFeeData.builder().id(fee.getId()).feeType(fee.getFeeType()).flatAmount(fee.getFlatAmount())
                    .percentage(fee.getPercentage()).currencyCode(fee.getCurrencyCode()).build();
        }
    }

    @Value
    @Builder
    public static class OverdraftUsageRuleData {

        Long id;
        String channel;
        BigDecimal perTransactionLimit;
        BigDecimal dailyLimit;
        BigDecimal monthlyLimit;
        boolean whitelist;
        Set<String> merchantCategoryCodes;

        public static OverdraftUsageRuleData from(OverdraftUsageRule rule) {
            return OverdraftUsageRuleData.builder().id(rule.getId()).channel(rule.getChannel())
                    .perTransactionLimit(rule.getPerTransactionLimit()).dailyLimit(rule.getDailyLimit())
                    .monthlyLimit(rule.getMonthlyLimit()).whitelist(rule.isWhitelist())
                    .merchantCategoryCodes(rule.getMerchantCategoryCodes()).build();
        }
    }

    @Value
    @Builder
    public static class OverdraftAccountingData {

        Long id;
        Long loanPortfolioAccountId;
        Long interestIncomeAccountId;
        Long feeIncomeAccountId;
        Long receivableInterestAccountId;
        Long receivableFeeAccountId;
        Long chargeOffExpenseAccountId;

        public static OverdraftAccountingData from(OverdraftProductAccounting accounting) {
            if (accounting == null) {
                return null;
            }
            return OverdraftAccountingData.builder().id(accounting.getId())
                    .loanPortfolioAccountId(extractId(accounting.getLoanPortfolioAccount()))
                    .interestIncomeAccountId(extractId(accounting.getInterestIncomeAccount()))
                    .feeIncomeAccountId(extractId(accounting.getFeeIncomeAccount()))
                    .receivableInterestAccountId(extractId(accounting.getReceivableInterestAccount()))
                    .receivableFeeAccountId(extractId(accounting.getReceivableFeeAccount()))
                    .chargeOffExpenseAccountId(extractId(accounting.getChargeOffExpenseAccount())).build();
        }

        private static Long extractId(GLAccount account) {
            return account != null ? account.getId() : null;
        }
    }
}
