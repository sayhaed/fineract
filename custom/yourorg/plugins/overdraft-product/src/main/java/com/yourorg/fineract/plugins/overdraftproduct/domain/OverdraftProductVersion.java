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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "m_overdraft_product_version")
public class OverdraftProductVersion extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private OverdraftProduct product;

    @Column(name = "version_label", nullable = false, length = 100)
    private String label;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_until")
    private LocalDate effectiveUntil;

    @Column(name = "default_limit", precision = 19, scale = 6)
    private BigDecimal defaultLimit;

    @Column(name = "interest_rate", precision = 19, scale = 6)
    private BigDecimal interestRate;

    @Column(name = "interest_calculation_period")
    private Integer interestCalculationPeriodType;

    @Column(name = "posting_strategy", length = 100)
    private String postingStrategy;

    @Column(name = "rounding_mode", length = 50)
    private String roundingMode;

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OverdraftFee> fees = new HashSet<>();

    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<OverdraftUsageRule> usageRules = new HashSet<>();

    @OneToOne(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OverdraftProductAccounting accounting;

    private OverdraftProductVersion(OverdraftProduct product, String label, LocalDate effectiveFrom, LocalDate effectiveUntil,
            BigDecimal defaultLimit, BigDecimal interestRate, Integer interestCalculationPeriodType, String postingStrategy,
            String roundingMode) {
        this.product = product;
        this.label = label;
        this.effectiveFrom = effectiveFrom;
        this.effectiveUntil = effectiveUntil;
        this.defaultLimit = defaultLimit;
        this.interestRate = interestRate;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.postingStrategy = postingStrategy;
        this.roundingMode = roundingMode;
    }

    public static OverdraftProductVersion of(OverdraftProduct product, String label, LocalDate effectiveFrom, LocalDate effectiveUntil,
            BigDecimal defaultLimit, BigDecimal interestRate, Integer interestCalculationPeriodType, String postingStrategy,
            String roundingMode) {
        return new OverdraftProductVersion(product, label, effectiveFrom, effectiveUntil, defaultLimit, interestRate,
                interestCalculationPeriodType, postingStrategy, roundingMode);
    }

    public void addFee(OverdraftFee fee) {
        fee.setVersion(this);
        this.fees.add(fee);
    }

    public void addUsageRule(OverdraftUsageRule rule) {
        rule.setVersion(this);
        this.usageRules.add(rule);
    }

    public void attachAccounting(OverdraftProductAccounting accounting) {
        accounting.setVersion(this);
        this.accounting = accounting;
    }

    public boolean isActiveOn(LocalDate businessDate) {
        boolean startsBefore = !businessDate.isBefore(effectiveFrom);
        boolean endsAfter = effectiveUntil == null || !businessDate.isAfter(effectiveUntil);
        return startsBefore && endsAfter;
    }
}
