package org.apache.fineract.portfolio.overdraftproduct.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_overdraft_product_version")
@Getter
@Setter
public class OverdraftProductVersion extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "overdraft_product_id", nullable = false)
    private OverdraftProduct product;

    @Column(name = "version_label", length = 100, nullable = false)
    private String versionLabel;

    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;

    @Column(name = "effective_to")
    private LocalDate effectiveTo;

    @Column(name = "notes", length = 1000)
    private String notes;

    public boolean isEffectiveOn(LocalDate date) {
        boolean starts = effectiveFrom == null || !effectiveFrom.isAfter(date);
        boolean ends = effectiveTo == null || !effectiveTo.isBefore(date);
        return starts && ends;
    }
}
