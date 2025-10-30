package org.apache.fineract.portfolio.overdraftproduct.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_overdraft_usage_rule")
@Getter
@Setter
public class OverdraftUsageRule extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overdraft_product_id", nullable = false)
    private OverdraftProduct product;

    @Column(name = "channel", length = 100, nullable = false)
    private String channel;

    @Column(name = "is_allowed", nullable = false)
    private boolean allowed;

    @ElementCollection
    @CollectionTable(name = "m_overdraft_usage_rule_whitelist", joinColumns = @JoinColumn(name = "usage_rule_id"))
    @Column(name = "merchant_category", length = 10)
    private Set<String> merchantCategoryWhitelist = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "m_overdraft_usage_rule_blacklist", joinColumns = @JoinColumn(name = "usage_rule_id"))
    @Column(name = "merchant_category", length = 10)
    private Set<String> merchantCategoryBlacklist = new HashSet<>();

    @Column(name = "transaction_limit", precision = 19, scale = 6)
    private BigDecimal transactionLimit;

    @Column(name = "daily_limit", precision = 19, scale = 6)
    private BigDecimal dailyLimit;

    @Column(name = "monthly_limit", precision = 19, scale = 6)
    private BigDecimal monthlyLimit;

    @Column(name = "cooling_period_minutes")
    private Integer coolingPeriodMinutes;
}
