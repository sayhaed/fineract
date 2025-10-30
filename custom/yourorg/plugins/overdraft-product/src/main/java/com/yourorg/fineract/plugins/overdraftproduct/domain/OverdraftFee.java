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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "m_overdraft_fee")
public class OverdraftFee extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "version_id", nullable = false)
    private OverdraftProductVersion version;

    @Column(name = "fee_type", nullable = false, length = 100)
    private String feeType;

    @Column(name = "flat_amount", precision = 19, scale = 6)
    private BigDecimal flatAmount;

    @Column(name = "percentage", precision = 19, scale = 6)
    private BigDecimal percentage;

    @Column(name = "currency_code", length = 10)
    private String currencyCode;

    private OverdraftFee(String feeType, BigDecimal flatAmount, BigDecimal percentage, String currencyCode) {
        this.feeType = feeType;
        this.flatAmount = flatAmount;
        this.percentage = percentage;
        this.currencyCode = currencyCode;
    }

    public static OverdraftFee of(String feeType, BigDecimal flatAmount, BigDecimal percentage, String currencyCode) {
        return new OverdraftFee(feeType, flatAmount, percentage, currencyCode);
    }
}
