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
package com.yourorg.fineract.plugins.overdraftproduct.service;

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductVersion;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductRepository;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductVersionRepository;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftUsageRuleRepository;
import com.yourorg.fineract.plugins.overdraftproduct.service.exception.OverdraftProductNotConfiguredException;
import com.yourorg.fineract.plugins.overdraftproduct.service.exception.OverdraftProductVersionNotFoundException;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRelatedDetail;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdraftRuleEngine {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductVersionRepository versionRepository;
    private final OverdraftUsageRuleRepository usageRuleRepository;

    @Transactional(readOnly = true)
    public void validateUsage(Loan loan, Money transactionAmount, String channel, String merchantCategoryCode, LocalDate businessDate) {
        OverdraftProduct product = productRepository.findByLoanProductId(loan.getLoanProduct().getId())
                .orElseThrow(() -> new OverdraftProductNotConfiguredException(loan.getLoanProduct().getId()));

        OverdraftProductVersion version = versionRepository.findActiveVersion(product, businessDate)
                .orElseThrow(() -> new OverdraftProductVersionNotFoundException(product.getId(), businessDate));

        ensureAmountWithinProductLimits(product, transactionAmount, loan.getLoanProductRelatedDetail());

        usageRuleRepository.findByVersionAndChannelIgnoreCase(version, channel)
                .ifPresent(rule -> rule.validate(loan, transactionAmount, merchantCategoryCode));
    }

    private void ensureAmountWithinProductLimits(OverdraftProduct product, Money transactionAmount,
            LoanProductRelatedDetail loanProductRelatedDetail) {
        Money minimum = product.getMinimumLimit() != null ? Money.of(loanProductRelatedDetail.getCurrency(), product.getMinimumLimit()) : null;
        Money maximum = product.getMaximumLimit() != null ? Money.of(loanProductRelatedDetail.getCurrency(), product.getMaximumLimit()) : null;

        if (minimum != null && transactionAmount.isLessThan(minimum)) {
            throw new PlatformApiDataValidationException(ApiParameterError.parameterError(
                    "validation.msg.overdraft.transaction.amount.below.minimum",
                    "Transaction amount is below overdraft minimum limit", "amount", minimum.getAmount()));
        }

        if (maximum != null && transactionAmount.isGreaterThan(maximum)) {
            throw new PlatformApiDataValidationException(ApiParameterError.parameterError(
                    "validation.msg.overdraft.transaction.amount.above.maximum",
                    "Transaction amount exceeds overdraft maximum limit", "amount", maximum.getAmount()));
        }
    }
}
