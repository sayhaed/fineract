package org.apache.fineract.portfolio.overdraftproduct.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Entity
@Table(name = "m_overdraft_product")
@Getter
@Setter
public class OverdraftProduct extends AbstractAuditableCustom {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_product_id")
    private LoanProduct loanProduct;

    @Column(name = "name", nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "default_limit", precision = 19, scale = 6, nullable = false)
    private BigDecimal defaultLimit;

    @Column(name = "min_limit", precision = 19, scale = 6, nullable = false)
    private BigDecimal minimumLimit;

    @Column(name = "max_limit", precision = 19, scale = 6, nullable = false)
    private BigDecimal maximumLimit;

    @Column(name = "interest_rate", precision = 10, scale = 6, nullable = false)
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_method", nullable = false, length = 50)
    private OverdraftInterestMethod interestMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_count_basis", nullable = false, length = 50)
    private OverdraftDayCountBasis dayCountBasis;

    @Column(name = "rounding_scale")
    private Integer roundingScale;

    @Enumerated(EnumType.STRING)
    @Column(name = "rounding_mode", length = 20)
    private OverdraftRoundingMode roundingMode;

    @Column(name = "compounding_enabled")
    private boolean compoundingEnabled;

    @Column(name = "posting_schedule", length = 255)
    private String postingSchedule;

    @Column(name = "allow_account_override")
    private boolean allowAccountOverride;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 25)
    private OverdraftProductStatus status = OverdraftProductStatus.DRAFT;

    @Column(name = "activated_on")
    private LocalDate activatedOn;

    @Column(name = "retired_on")
    private LocalDate retiredOn;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OverdraftFee> fees = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OverdraftUsageRule> usageRules = new ArrayList<>();

    @OneToOne(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OverdraftProductAccounting accounting;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("effectiveFrom ASC")
    private List<OverdraftProductVersion> versions = new ArrayList<>();

    public void activate(LocalDate activationDate) {
        this.status = OverdraftProductStatus.ACTIVE;
        this.activatedOn = activationDate;
        this.retiredOn = null;
    }

    public void retire(LocalDate retirementDate) {
        this.status = OverdraftProductStatus.RETIRED;
        this.retiredOn = retirementDate;
    }

    public boolean isActiveOn(LocalDate businessDate) {
        if (!status.isActive()) {
            return false;
        }
        return versions.stream().anyMatch(version -> version.isEffectiveOn(businessDate));
    }

    public void addFee(OverdraftFee fee) {
        Objects.requireNonNull(fee, "Overdraft fee must be provided");
        fee.setProduct(this);
        this.fees.add(fee);
    }

    public void clearFees() {
        this.fees.forEach(fee -> fee.setProduct(null));
        this.fees.clear();
    }

    public void addUsageRule(OverdraftUsageRule rule) {
        Objects.requireNonNull(rule, "Usage rule must be provided");
        rule.setProduct(this);
        this.usageRules.add(rule);
    }

    public void clearUsageRules() {
        this.usageRules.forEach(rule -> rule.setProduct(null));
        this.usageRules.clear();
    }

    public void setAccounting(OverdraftProductAccounting accounting) {
        if (accounting == null) {
            if (this.accounting != null) {
                this.accounting.setProduct(null);
            }
            this.accounting = null;
        } else {
            accounting.setProduct(this);
            this.accounting = accounting;
        }
    }

    public void addVersion(OverdraftProductVersion version) {
        Objects.requireNonNull(version, "Version must be provided");
        version.setProduct(this);
        this.versions.add(version);
    }

    public void clearVersions() {
        this.versions.forEach(version -> version.setProduct(null));
        this.versions.clear();
    }
}
