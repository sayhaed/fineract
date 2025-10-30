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
package com.yourorg.fineract.plugins.overdraftproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "m_overdraft_usage_rule")
public class OverdraftUsageRule extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private OverdraftProductVersion version;

    @Column(name = "channel", nullable = false, length = 100)
    private String channel;

    @Column(name = "per_txn_limit", precision = 19, scale = 6)
    private BigDecimal perTransactionLimit;

    @Column(name = "daily_limit", precision = 19, scale = 6)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 6)
    private BigDecimal monthlyLimit;

    @Column(name = "whitelist")
    private boolean whitelist;

    @Column(name = "mcc_codes", length = 400)
    private String merchantCategoryCodeCsv;

    private OverdraftUsageRule(String channel, BigDecimal perTransactionLimit, BigDecimal dailyLimit, BigDecimal monthlyLimit,
            boolean whitelist, Set<String> merchantCategoryCodes) {
        this.channel = channel;
        this.perTransactionLimit = perTransactionLimit;
        this.dailyLimit = dailyLimit;
        this.monthlyLimit = monthlyLimit;
        this.whitelist = whitelist;
        setMerchantCategoryCodes(merchantCategoryCodes);
    }

    public static OverdraftUsageRule of(String channel, BigDecimal perTransactionLimit, BigDecimal dailyLimit, BigDecimal monthlyLimit,
            boolean whitelist, Set<String> merchantCategoryCodes) {
        return new OverdraftUsageRule(channel, perTransactionLimit, dailyLimit, monthlyLimit, whitelist, merchantCategoryCodes);
    }

    public void setMerchantCategoryCodes(Set<String> merchantCategoryCodes) {
        if (merchantCategoryCodes == null || merchantCategoryCodes.isEmpty()) {
            this.merchantCategoryCodeCsv = null;
        } else {
            this.merchantCategoryCodeCsv = merchantCategoryCodes.stream().map(String::trim).filter(s -> !s.isEmpty())
                    .collect(Collectors.joining(","));
        }
    }

    public Set<String> getMerchantCategoryCodes() {
        if (merchantCategoryCodeCsv == null || merchantCategoryCodeCsv.isBlank()) {
            return Collections.emptySet();
        }
        return new HashSet<>(Arrays.asList(merchantCategoryCodeCsv.split(",")));
    }

    public void validate(Loan loan, Money transactionAmount, String merchantCategoryCode) {
        List<ApiParameterError> errors = new ArrayList<>();
        DataValidatorBuilder validator = new DataValidatorBuilder(errors).resource("overdraftUsageRule").parameter("channel")
                .value(channel);
        validator.notBlank();

        if (perTransactionLimit != null) {
            LoanProductRelatedDetail loanProductRelatedDetail = loan.getLoanProductRelatedDetail();
            Money limit = Money.of(loanProductRelatedDetail.getCurrency(), perTransactionLimit);
            if (transactionAmount.isGreaterThan(limit)) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraft.transaction.limit.exceeded",
                        "Per transaction limit exceeded", "perTransactionLimit", perTransactionLimit));
            }
        }

        if (!errors.isEmpty()) {
            throw new PlatformApiDataValidationException(errors);
        }

        if (!getMerchantCategoryCodes().isEmpty()) {
            boolean contains = getMerchantCategoryCodes().contains(merchantCategoryCode);
            if (whitelist && !contains) {
                throw new OverdraftUsageRuleViolationException("validation.msg.overdraft.channel.not.whitelisted",
                        "Channel is not whitelisted for MCC", merchantCategoryCode);
            } else if (!whitelist && contains) {
                throw new OverdraftUsageRuleViolationException("validation.msg.overdraft.channel.blacklisted",
                        "Channel is blacklisted for MCC", merchantCategoryCode);
            }
        }
    }

    public boolean supportsChannel(String requestChannel) {
        return this.channel.equalsIgnoreCase(requestChannel);
    }

    public static class OverdraftUsageRuleViolationException extends AbstractPlatformResourceNotFoundException {

        public OverdraftUsageRuleViolationException(String globalisationMessageCode, String defaultUserMessage, String parameterName) {
            super(globalisationMessageCode, defaultUserMessage, parameterName);
        }
    }
}
