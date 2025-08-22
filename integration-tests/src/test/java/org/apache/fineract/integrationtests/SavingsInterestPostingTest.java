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
package org.apache.fineract.integrationtests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.integrationtests.common.*;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsInterestPostingTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsInterestPostingTest.class);
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper schedulerJobHelper;
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    private GlobalConfigurationHelper globalConfigurationHelper;
    private SavingsProductHelper productHelper;
    private Long betweenDays;
    private JournalEntryHelper JOURNAL_ENTRY_HELPER;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.JOURNAL_ENTRY_HELPER = new JournalEntryHelper(this.requestSpec, this.responseSpec);
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testPostInterestWithOverdraftProduct() {

        try {
            final String amount = "10000";
            final String jobName = "Post Interest For Savings";
            // --- ARRANGE ---
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = this.accountHelper.createLiabilityAccount("Interest Payable");

            final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 2, 1);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
            final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductID,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
            this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
            this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, amount, startDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            // Simulate time passing - update business date to March
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate marchDate = LocalDate.of(LocalDate.now().getYear(), 3, 1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, marchDate);

            schedulerJobHelper.executeAndAwaitJob(jobName);
            betweenDays = ChronoUnit.DAYS.between(startDate, marchDate);
            List<HashMap> interestTransactions = getInterestTransactions(savingsAccountId);
            BigDecimal amountInterest = getCalculateInterestPostingForDay(productHelper, amount);

            for (HashMap interest : interestTransactions) {
                Assertions.assertEquals(amountInterest, interest.get("amount"));
            }

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void testOverdraftInterestWithOverdraftProduct() {

        try {
            final String amount = "10000";
            final String jobName = "Post Interest For Savings";
            // --- ARRANGE ---
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = this.accountHelper.createLiabilityAccount("Interest Payable");

            final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 2, 1);
            final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
            final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductID,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
            this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
            this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsAccountId, "10000", startDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            // Simulate time passing - update business date to March
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate marchDate = LocalDate.of(LocalDate.now().getYear(), 3, 1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, marchDate);

            schedulerJobHelper.executeAndAwaitJob(jobName);
            betweenDays = ChronoUnit.DAYS.between(startDate, marchDate);
            List<HashMap> interestTransactions = getInterestTransactions(savingsAccountId);
            BigDecimal amountInterest = getCalculateOverdraftPostingForDay(productHelper, amount);

            BigDecimal transacctionAmount = BigDecimal.valueOf(((Double) interestTransactions.get(0).get("amount")));
            Assertions.assertEquals(amountInterest, transacctionAmount);

            BigDecimal runningBalance = BigDecimal.valueOf(((Double) interestTransactions.get(0).get("runningBalance")));
            Boolean isLessBalance = MathUtil.isLessThanZero(runningBalance);
            Assertions.assertTrue(isLessBalance, "Running balance is not less than zero");

            Integer id = ((Double) interestTransactions.get(0).get("id")).intValue();
            List<HashMap> journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("S" + id);

            boolean debitFound = false;
            boolean creditFound = false;
            for (Map<String, Object> entry : journalEntries) {
                String entryType = (String) ((HashMap) entry.get("entryType")).get("value");
                Integer accountId = ((Number) entry.get("glAccountId")).intValue();
                if ("DEBIT".equals(entryType) && accountId.equals(assetAccount.getAccountID())) {
                    debitFound = true;
                }
                if ("CREDIT".equals(entryType) && accountId.equals(interestReceivableAccount.getAccountID())) {
                    creditFound = true;
                }
            }

            Assertions.assertTrue(creditFound, "CREDIT to Interest Receivable (Asset) Account not found for negative .");
            Assertions.assertTrue(debitFound, "DEBITto Overdraft portfolio (Asset) not found for negative.");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    /*@Test
    public void testOverdraftAndInterestPosting_WithOverdraftProduct() {

        try {
            final String amountDeposit = "10000";
            final String amountWithdrawal = "20000";
            final String jobName = "Post Interest For Savings";
            // --- ARRANGE ---
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
            final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
            final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");
            final Account interestPayableAccount = this.accountHelper.createLiabilityAccount("Interest Payable");

            final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(
                    interestPayableAccount.getAccountID().toString(), savingsControlAccount.getAccountID().toString(),
                    interestReceivableAccount.getAccountID().toString(), assetAccount, incomeAccount, expenseAccount, liabilityAccount);

            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2025");
            final LocalDate startDate = LocalDate.of(LocalDate.now().getYear(), 2, 1);
            final String depositDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
            final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductID,
                    SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, depositDateString);
            this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, depositDateString);
            this.savingsAccountHelper.activateSavings(savingsAccountId, depositDateString);
            this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, amountDeposit, depositDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            final LocalDate withdrawalDate = LocalDate.of(LocalDate.now().getYear(), 2, 16);
            final String withdrawalDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(withdrawalDate);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsAccountId, amountWithdrawal, withdrawalDateString,
                    CommonConstants.RESPONSE_RESOURCE_ID);

            // Simulate time passing - update business date to March
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            LocalDate marchDate = LocalDate.of(LocalDate.now().getYear(), 3, 1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, marchDate);

            schedulerJobHelper.executeAndAwaitJob(jobName);

            List<HashMap> interestTransactions = getInterestTransactions(savingsAccountId);
            /*
             * The withdrawal is considered because it was the last transaction, and we need to use the running balance
             * to calculate the interest, since a withdrawal adjusts the balance.

            List<HashMap> transactions = getTransactionsWithdrawal(savingsAccountId);
            BigDecimal runningBalanceWithdrawal = BigDecimal.valueOf(((Double) transactions.get(0).get("runningBalance")));

            for (HashMap transaction : interestTransactions) {
                BigDecimal transacctionAmount = BigDecimal.valueOf(((Double) transaction.get("amount")));
                Map<String, Object> transactionType = (Map<String, Object>) transaction.get("transactionType");
                SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) transactionType.get("id")).intValue());
                if (type.isInterestPosting()) {
                    betweenDays = ChronoUnit.DAYS.between(startDate, withdrawalDate);
                    BigDecimal amountInterest = getCalculateInterestPostingForDay(productHelper, amountDeposit);
                    Assertions.assertEquals(amountInterest, transacctionAmount);
                } else {
                    betweenDays = ChronoUnit.DAYS.between(withdrawalDate, marchDate);
                    BigDecimal amountInterest = getCalculateOverdraftPostingForDay(productHelper,
                            runningBalanceWithdrawal.negate().toString());
                    Assertions.assertEquals(amountInterest, transacctionAmount);
                }
            }

            BigDecimal runningBalance = BigDecimal.valueOf(((Double) interestTransactions.get(0).get("runningBalance")));
            Boolean isLessBalance = MathUtil.isLessThanZero(runningBalance);
            Assertions.assertTrue(isLessBalance, "Running balance is not less than zero");

            Integer id = ((Double) interestTransactions.get(0).get("id")).intValue();
            List<HashMap> journalEntries = JOURNAL_ENTRY_HELPER.getJournalEntriesByTransactionId("S" + id);

            boolean debitFound = false;
            boolean creditFound = false;
            for (Map<String, Object> entry : journalEntries) {
                String entryType = (String) ((HashMap) entry.get("entryType")).get("value");
                Integer accountId = ((Number) entry.get("glAccountId")).intValue();
                if ("DEBIT".equals(entryType) && accountId.equals(assetAccount.getAccountID())) {
                    debitFound = true;
                }
                if ("CREDIT".equals(entryType) && accountId.equals(interestReceivableAccount.getAccountID())) {
                    creditFound = true;
                }
            }

            Assertions.assertTrue(creditFound, "CREDIT to Interest Receivable (Asset) Account not found for negative .");
            Assertions.assertTrue(debitFound, "DEBITto Overdraft portfolio (Asset) not found for negative.");

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }*/

    public Integer createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(final String interestPayableAccount,
            final String savingsControlAccount, final String interestReceivableAccount, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITHOUT OVERDRAFT ---------------------------------------");
        this.productHelper = new SavingsProductHelper().withOverDraftRate("100000", "21")
                .withAccountInterestReceivables(interestReceivableAccount).withSavingsControlAccountId(savingsControlAccount)
                .withInterestPayableAccountId(interestPayableAccount).withInterestCompoundingPeriodTypeAsAnnually() //
                .withInterestPostingPeriodTypeAsMonthly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withAccountingRuleAsAccrualBased(accounts);
        final String savingsProductJSON = this.productHelper.build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private List<HashMap> getInterestTransactions(Integer savingsAccountId) {
        List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
        List<HashMap> interestPostingTransactions = new ArrayList<>();
        for (HashMap transaction : allTransactions) {
            Map<String, Object> transactionType = (Map<String, Object>) transaction.get("transactionType");
            SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) transactionType.get("id")).intValue());
            if (type.isInterestPosting() || type.isOverDraftInterestPosting()) {
                interestPostingTransactions.add(transaction);
            }
        }
        return interestPostingTransactions;
    }

    private List<HashMap> getTransactionsWithdrawal(Integer savingsAccountId) {
        List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
        List<HashMap> interestPostingTransactions = new ArrayList<>();
        for (HashMap transaction : allTransactions) {
            Map<String, Object> transactionType = (Map<String, Object>) transaction.get("transactionType");
            SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) transactionType.get("id")).intValue());
            if (type.isWithdrawal()) {
                interestPostingTransactions.add(transaction);
            }
        }
        return interestPostingTransactions;
    }

    private BigDecimal getCalculateInterestPostingForDay(SavingsProductHelper productHelper, String amount) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal interestRateAsFraction = productHelper.getNominalAnnualInterestRate().divide(new BigDecimal(100.00));
        BigDecimal realBalanceForInterestCalculation = new BigDecimal(amount);

        final BigDecimal multiplicand = BigDecimal.ONE.divide(productHelper.getInterestCalculationDaysInYearType(), MathContext.DECIMAL64);
        final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        final BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(betweenDays), MathContext.DECIMAL64);
        interest = realBalanceForInterestCalculation.multiply(periodicInterestRate, MathContext.DECIMAL64)
                .setScale(productHelper.getDecimalCurrency(), RoundingMode.HALF_EVEN);

        return interest;
    }

    private BigDecimal getCalculateOverdraftPostingForDay(SavingsProductHelper productHelper, String amount) {
        BigDecimal interest = BigDecimal.ZERO;
        BigDecimal interestRateAsFraction = productHelper.getNominalAnnualInterestRateOverdraft().divide(new BigDecimal(100.00));
        BigDecimal realBalanceForInterestCalculation = new BigDecimal(amount);

        final BigDecimal multiplicand = BigDecimal.ONE.divide(productHelper.getInterestCalculationDaysInYearType(), MathContext.DECIMAL64);
        final BigDecimal dailyInterestRate = interestRateAsFraction.multiply(multiplicand, MathContext.DECIMAL64);
        final BigDecimal periodicInterestRate = dailyInterestRate.multiply(BigDecimal.valueOf(betweenDays), MathContext.DECIMAL64);
        interest = realBalanceForInterestCalculation.multiply(periodicInterestRate, MathContext.DECIMAL64)
                .setScale(productHelper.getDecimalCurrency(), RoundingMode.HALF_EVEN);

        return interest;
    }

}
