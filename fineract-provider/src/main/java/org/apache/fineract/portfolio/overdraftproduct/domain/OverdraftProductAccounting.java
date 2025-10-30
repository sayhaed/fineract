package org.apache.fineract.portfolio.overdraftproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_overdraft_product_accounting")
@Getter
@Setter
public class OverdraftProductAccounting extends AbstractPersistableCustom<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overdraft_product_id", nullable = false, unique = true)
    private OverdraftProduct product;

    @JoinColumn(name = "receivable_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount receivableAccount;

    @JoinColumn(name = "interest_income_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount interestIncomeAccount;

    @JoinColumn(name = "fee_income_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount feeIncomeAccount;

    @JoinColumn(name = "suspense_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount suspenseAccount;

    @JoinColumn(name = "writeoff_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount writeOffAccount;

    @JoinColumn(name = "penalty_income_account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private GLAccount penaltyIncomeAccount;

    @Column(name = "accrual_enabled")
    private boolean accrualEnabled;
}
