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

import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftFee;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProduct;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductAccounting;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftProductVersion;
import com.yourorg.fineract.plugins.overdraftproduct.domain.OverdraftUsageRule;
import com.yourorg.fineract.plugins.overdraftproduct.domain.repositories.OverdraftProductRepository;
import com.yourorg.fineract.plugins.overdraftproduct.serialization.OverdraftProductCommand;
import com.yourorg.fineract.plugins.overdraftproduct.service.exception.OverdraftProductNotFoundException;
import com.yourorg.fineract.portfolio.loanproduct.domain.LoanProduct;
import com.yourorg.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import com.yourorg.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OverdraftProductWritePlatformServiceImpl implements OverdraftProductWritePlatformService {

    private final PlatformSecurityContext securityContext;
    private final OverdraftProductValidationService validationService;
    private final OverdraftProductRepository productRepository;
    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    @Override
    @Transactional
    public Long createProduct(OverdraftProductCommand command) {
        securityContext.authenticatedUser();
        validationService.validateForCreate(command);

        LoanProduct loanProduct = loanProductRepository.findById(command.getLoanProductId())
                .orElseThrow(() -> new LoanProductNotFoundException(command.getLoanProductId()));

        OverdraftProduct product = OverdraftProduct.of(loanProduct, command.getExternalId(), command.getName(), command.getDescription(),
                command.getMinimumLimit(), command.getMaximumLimit(), command.getDefaultLimit(), command.getEffectiveFrom(),
                command.getEffectiveUntil());

        OverdraftProductVersion version = buildVersion(product, command);
        product.addVersion(version);

        productRepository.save(product);
        return product.getId();
    }

    @Override
    @Transactional
    public Long updateProduct(Long productId, OverdraftProductCommand command) {
        securityContext.authenticatedUser();
        validationService.validateForCreate(command);

        OverdraftProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new OverdraftProductNotFoundException(productId));

        product.setName(command.getName());
        product.setDescription(command.getDescription());
        product.setExternalId(command.getExternalId());
        product.setMinimumLimit(command.getMinimumLimit());
        product.setMaximumLimit(command.getMaximumLimit());
        product.setDefaultLimit(command.getDefaultLimit());
        product.setValidFrom(command.getEffectiveFrom());
        product.setValidUntil(command.getEffectiveUntil());

        OverdraftProductVersion version = buildVersion(product, command);
        product.addVersion(version);

        productRepository.save(product);
        return product.getId();
    }

    @Override
    @Transactional
    public void activate(Long productId) {
        securityContext.authenticatedUser();
        OverdraftProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new OverdraftProductNotFoundException(productId));
        product.activate();
    }

    @Override
    @Transactional
    public void retire(Long productId) {
        securityContext.authenticatedUser();
        OverdraftProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new OverdraftProductNotFoundException(productId));
        product.retire();
    }

    private OverdraftProductVersion buildVersion(OverdraftProduct product, OverdraftProductCommand command) {
        String label = StringUtils.defaultIfBlank(command.getVersionLabel(), "v" + (product.getVersions().size() + 1));
        OverdraftProductVersion version = OverdraftProductVersion.of(product, label, command.getEffectiveFrom(),
                command.getEffectiveUntil(), command.getDefaultLimit(), command.getInterestRate(),
                command.getInterestCalculationPeriodType(), command.getPostingStrategy(), command.getRoundingMode());

        command.getFees().forEach(feeCommand -> version
                .addFee(OverdraftFee.of(feeCommand.getFeeType(), feeCommand.getFlatAmount(), feeCommand.getPercentage(), feeCommand.getCurrencyCode())));
        command.getUsageRules().forEach(ruleCommand -> version.addUsageRule(OverdraftUsageRule.of(ruleCommand.getChannel(),
                ruleCommand.getPerTransactionLimit(), ruleCommand.getDailyLimit(), ruleCommand.getMonthlyLimit(),
                ruleCommand.isWhitelist(), ruleCommand.getMerchantCategoryCodes())));

        if (command.getAccounting() != null) {
            OverdraftProductAccounting accounting = OverdraftProductAccounting.of(resolveAccount(command.getAccounting().getLoanPortfolioAccountId()),
                    resolveAccount(command.getAccounting().getInterestIncomeAccountId()),
                    resolveAccount(command.getAccounting().getFeeIncomeAccountId()),
                    resolveAccount(command.getAccounting().getReceivableInterestAccountId()),
                    resolveAccount(command.getAccounting().getReceivableFeeAccountId()),
                    resolveAccount(command.getAccounting().getChargeOffExpenseAccountId()));
            version.attachAccounting(accounting);
        }

        return version;
    }

    private GLAccount resolveAccount(Long accountId) {
        if (accountId == null) {
            return null;
        }
        return glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountId);
    }
}
