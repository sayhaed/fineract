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
package com.yourorg.fineract.plugins.overdraftproduct.spi;

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductVersion;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductRepository;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductVersionRepository;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestConfigOverrideProvider {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductVersionRepository versionRepository;

    @PostConstruct
    public void logRegistration() {
        log.info("Overdraft interest configuration override provider ready");
    }

    public void applyOverrides(Loan loan) {
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        Optional<OverdraftProduct> productOptional = productRepository.findByLoanProductId(loan.getLoanProduct().getId());
        if (productOptional.isEmpty()) {
            return;
        }
        versionRepository.findActiveVersion(productOptional.get(), businessDate).ifPresent(version -> updateDetail(loan, version));
    }

    private void updateDetail(Loan loan, OverdraftProductVersion version) {
        LoanProductRelatedDetail detail = loan.getLoanProductRelatedDetail();
        if (version.getInterestRate() != null) {
            BigDecimal rate = version.getInterestRate();
            detail.setNominalInterestRatePerPeriod(rate);
            detail.setAnnualNominalInterestRate(rate);
        }
        if (version.getInterestCalculationPeriodType() != null) {
            detail.setInterestCalculationPeriodMethod(InterestCalculationPeriodMethod.fromInt(version.getInterestCalculationPeriodType()));
        }
        if (version.getPostingStrategy() != null) {
            try {
                detail.setLoanScheduleProcessingType(LoanScheduleProcessingType.valueOf(version.getPostingStrategy()));
            } catch (IllegalArgumentException ex) {
                log.warn("Unsupported posting strategy {} configured for overdraft product {}", version.getPostingStrategy(),
                        version.getProduct().getId());
            }
        }
        if (version.getRoundingMode() != null) {
            log.debug("Rounding mode override {} configured for overdraft product {} but currency rounding is immutable in core",
                    version.getRoundingMode(), version.getProduct().getId());
        }
    }
}
