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
package com.yourorg.fineract.plugins.overdraftproduct.accounting;

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductAccounting;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductVersion;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductAccountingRepository;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductRepository;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductVersionRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdraftAccountingResolver {

    private final OverdraftProductRepository productRepository;
    private final OverdraftProductVersionRepository versionRepository;
    private final OverdraftProductAccountingRepository accountingRepository;

    public Optional<OverdraftProductAccounting> resolveFor(Loan loan) {
        Optional<OverdraftProduct> productOptional = productRepository.findByLoanProductId(loan.getLoanProduct().getId());
        if (productOptional.isEmpty()) {
            return Optional.empty();
        }
        LocalDate businessDate = DateUtils.getBusinessLocalDate();
        Optional<OverdraftProductVersion> versionOptional = versionRepository.findActiveVersion(productOptional.get(), businessDate);
        return versionOptional.flatMap(accountingRepository::findByVersion);
    }
}
