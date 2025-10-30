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

import com.yourorg.fineract.plugins.overdraftproduct.service.OverdraftRuleEngine;
import com.yourorg.fineract.plugins.overdraftproduct.spi.InterestConfigOverrideProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.event.business.BusinessEventListener;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OverdraftLoanTxnValidator implements BusinessEventListener<LoanTransactionBusinessEvent> {

    private final BusinessEventNotifierService businessEventNotifierService;
    private final OverdraftRuleEngine ruleEngine;
    private final InterestConfigOverrideProvider interestConfigOverrideProvider;

    @PostConstruct
    public void register() {
        businessEventNotifierService.addPreBusinessEventListener(LoanTransactionBusinessEvent.class, this);
        log.info("Registered overdraft loan transaction validator");
    }

    @Override
    public void onBusinessEvent(LoanTransactionBusinessEvent event) {
        LoanTransaction transaction = event.get();
        Loan loan = transaction.getLoan();
        interestConfigOverrideProvider.applyOverrides(loan);
        Money amount = transaction.getAmount(loan.getLoanProductRelatedDetail().getCurrency());
        String channel = resolveChannel(transaction.getPaymentDetail());
        String merchantCategoryCode = resolveMerchantCategoryCode(transaction.getPaymentDetail());
        ruleEngine.validateUsage(loan, amount, channel, merchantCategoryCode, DateUtils.getBusinessLocalDate());
    }

    private String resolveChannel(PaymentDetail paymentDetail) {
        if (paymentDetail == null || paymentDetail.getPaymentType() == null) {
            return "UNSPECIFIED";
        }
        return paymentDetail.getPaymentType().getName();
    }

    private String resolveMerchantCategoryCode(PaymentDetail paymentDetail) {
        if (paymentDetail == null) {
            return null;
        }
        return paymentDetail.getRoutingCode();
    }
}
