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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "m_overdraft_product_accounting")
public class OverdraftProductAccounting extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false, unique = true)
    private OverdraftProductVersion version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_portfolio_account_id")
    private GLAccount loanPortfolioAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "interest_income_account_id")
    private GLAccount interestIncomeAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_income_account_id")
    private GLAccount feeIncomeAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_interest_account_id")
    private GLAccount receivableInterestAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receivable_fee_account_id")
    private GLAccount receivableFeeAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_off_account_id")
    private GLAccount chargeOffExpenseAccount;

    private OverdraftProductAccounting(GLAccount loanPortfolioAccount, GLAccount interestIncomeAccount, GLAccount feeIncomeAccount,
            GLAccount receivableInterestAccount, GLAccount receivableFeeAccount, GLAccount chargeOffExpenseAccount) {
        this.loanPortfolioAccount = loanPortfolioAccount;
        this.interestIncomeAccount = interestIncomeAccount;
        this.feeIncomeAccount = feeIncomeAccount;
        this.receivableInterestAccount = receivableInterestAccount;
        this.receivableFeeAccount = receivableFeeAccount;
        this.chargeOffExpenseAccount = chargeOffExpenseAccount;
    }

    public static OverdraftProductAccounting of(GLAccount loanPortfolioAccount, GLAccount interestIncomeAccount,
            GLAccount feeIncomeAccount, GLAccount receivableInterestAccount, GLAccount receivableFeeAccount,
            GLAccount chargeOffExpenseAccount) {
        return new OverdraftProductAccounting(loanPortfolioAccount, interestIncomeAccount, feeIncomeAccount, receivableInterestAccount,
                receivableFeeAccount, chargeOffExpenseAccount);
    }
}
