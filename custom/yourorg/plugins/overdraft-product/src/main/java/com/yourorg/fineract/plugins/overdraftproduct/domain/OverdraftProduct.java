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
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "m_overdraft_product")
public class OverdraftProduct extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loan_product_id", nullable = false, unique = true)
    private LoanProduct loanProduct;

    @Column(name = "external_id", length = 100)
    private String externalId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "limit_min", precision = 19, scale = 6)
    private BigDecimal minimumLimit;

    @Column(name = "limit_max", precision = 19, scale = 6)
    private BigDecimal maximumLimit;

    @Column(name = "limit_default", precision = 19, scale = 6)
    private BigDecimal defaultLimit;

    @Column(name = "active")
    private boolean active;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_until")
    private LocalDate validUntil;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OverdraftProductVersion> versions = new ArrayList<>();

    private OverdraftProduct(LoanProduct loanProduct, String externalId, String name, String description, BigDecimal minimumLimit,
            BigDecimal maximumLimit, BigDecimal defaultLimit, LocalDate validFrom, LocalDate validUntil) {
        this.loanProduct = loanProduct;
        this.externalId = externalId;
        this.name = name;
        this.description = description;
        this.minimumLimit = minimumLimit;
        this.maximumLimit = maximumLimit;
        this.defaultLimit = defaultLimit;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.active = false;
    }

    public static OverdraftProduct of(LoanProduct loanProduct, String externalId, String name, String description, BigDecimal minimumLimit,
            BigDecimal maximumLimit, BigDecimal defaultLimit, LocalDate validFrom, LocalDate validUntil) {
        return new OverdraftProduct(loanProduct, externalId, name, description, minimumLimit, maximumLimit, defaultLimit, validFrom,
                validUntil);
    }

    public void activate() {
        this.active = true;
    }

    public void retire() {
        this.active = false;
    }

    public List<OverdraftProductVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    public void addVersion(OverdraftProductVersion version) {
        version.setProduct(this);
        this.versions.add(version);
    }

    public void replaceVersions(List<OverdraftProductVersion> newVersions) {
        this.versions.clear();
        newVersions.forEach(this::addVersion);
    }
}
