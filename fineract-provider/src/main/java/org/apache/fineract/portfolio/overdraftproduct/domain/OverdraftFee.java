package org.apache.fineract.portfolio.overdraftproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_overdraft_fee")
@Getter
@Setter
public class OverdraftFee extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overdraft_product_id", nullable = false)
    private OverdraftProduct product;

    @Enumerated(EnumType.STRING)
    @Column(name = "fee_type", nullable = false, length = 50)
    private OverdraftFeeType feeType;

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 50)
    private OverdraftFeeCalculationType calculationType;

    @Column(name = "amount", precision = 19, scale = 6)
    private BigDecimal amount;

    @Column(name = "percentage", precision = 10, scale = 6)
    private BigDecimal percentage;

    @Column(name = "tier_definition", length = 1000)
    private String tierDefinition;

    @Column(name = "trigger_condition", length = 255)
    private String triggerCondition;

    @Column(name = "is_active")
    private boolean active = true;
}
