package org.apache.fineract.portfolio.overdraftproduct.serialization;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.glaccount.domain.GLAccountRepositoryWrapper;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFee;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductAccounting;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductVersion;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftUsageRule;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftFeeCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductAccountingCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductVersionCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftUsageRuleCommand;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OverdraftProductMapper {

    private final LoanProductRepository loanProductRepository;
    private final GLAccountRepositoryWrapper glAccountRepositoryWrapper;

    public OverdraftProduct mapToNewEntity(OverdraftProductCommand command) {
        OverdraftProduct product = new OverdraftProduct();
        updateEntity(product, command);
        return product;
    }

    public void updateEntity(OverdraftProduct product, OverdraftProductCommand command) {
        product.setName(command.getName());
        product.setDescription(command.getDescription());
        product.setCurrencyCode(command.getCurrencyCode());
        product.setDefaultLimit(command.getDefaultLimit());
        product.setMinimumLimit(command.getMinimumLimit());
        product.setMaximumLimit(command.getMaximumLimit());
        product.setInterestRate(command.getInterestRate());
        product.setInterestMethod(command.getInterestMethod());
        product.setDayCountBasis(command.getDayCountBasis());
        product.setRoundingScale(command.getRoundingScale());
        product.setRoundingMode(command.getRoundingMode());
        product.setCompoundingEnabled(command.isCompoundingEnabled());
        product.setPostingSchedule(command.getPostingSchedule());
        product.setAllowAccountOverride(command.isAllowAccountOverride());

        if (command.getLoanProductId() != null) {
            LoanProduct loanProduct = loanProductRepository.findById(command.getLoanProductId())
                    .orElseThrow(() -> new LoanProductNotFoundException(command.getLoanProductId()));
            product.setLoanProduct(loanProduct);
        } else {
            product.setLoanProduct(null);
        }

        replaceFees(product, command.getFees());
        replaceUsageRules(product, command.getUsageRules());
        updateAccounting(product, command.getAccounting());
        replaceVersions(product, command.getVersions());
    }

    public OverdraftProductData mapToData(OverdraftProduct product) {
        List<OverdraftFeeData> feeData = product.getFees().stream().map(this::mapFee).collect(Collectors.toList());
        List<OverdraftUsageRuleData> usageRuleData = product.getUsageRules().stream().map(this::mapUsageRule)
                .collect(Collectors.toList());
        List<OverdraftProductVersionData> versionData = product.getVersions().stream().map(this::mapVersion)
                .collect(Collectors.toList());
        return OverdraftProductData.builder().id(product.getId())
                .loanProductId(product.getLoanProduct() != null ? product.getLoanProduct().getId() : null)
                .name(product.getName()).description(product.getDescription()).currencyCode(product.getCurrencyCode())
                .defaultLimit(product.getDefaultLimit()).minimumLimit(product.getMinimumLimit())
                .maximumLimit(product.getMaximumLimit()).interestRate(product.getInterestRate())
                .interestMethod(product.getInterestMethod()).dayCountBasis(product.getDayCountBasis())
                .roundingScale(product.getRoundingScale()).roundingMode(product.getRoundingMode())
                .compoundingEnabled(product.isCompoundingEnabled()).postingSchedule(product.getPostingSchedule())
                .allowAccountOverride(product.isAllowAccountOverride()).status(product.getStatus())
                .activatedOn(product.getActivatedOn()).retiredOn(product.getRetiredOn()).fees(feeData)
                .usageRules(usageRuleData).accounting(mapAccounting(product.getAccounting())).versions(versionData).build();
    }

    public void replaceFees(OverdraftProduct product, List<OverdraftFeeCommand> feeCommands) {
        product.clearFees();
        if (feeCommands == null) {
            return;
        }
        feeCommands.forEach(feeCommand -> {
            OverdraftFee fee = new OverdraftFee();
            fee.setFeeType(feeCommand.getFeeType());
            fee.setCalculationType(feeCommand.getCalculationType());
            fee.setAmount(feeCommand.getAmount());
            fee.setPercentage(feeCommand.getPercentage());
            fee.setTierDefinition(feeCommand.getTierDefinition());
            fee.setTriggerCondition(feeCommand.getTriggerCondition());
            fee.setActive(feeCommand.getActive() == null || Boolean.TRUE.equals(feeCommand.getActive()));
            product.addFee(fee);
        });
    }

    public void replaceUsageRules(OverdraftProduct product, List<OverdraftUsageRuleCommand> ruleCommands) {
        product.clearUsageRules();
        if (ruleCommands == null) {
            return;
        }
        ruleCommands.forEach(ruleCommand -> {
            OverdraftUsageRule rule = new OverdraftUsageRule();
            rule.setChannel(ruleCommand.getChannel());
            rule.setAllowed(Boolean.TRUE.equals(ruleCommand.getAllowed()));
            rule.setMerchantCategoryWhitelist(copySet(ruleCommand.getMerchantCategoryWhitelist()));
            rule.setMerchantCategoryBlacklist(copySet(ruleCommand.getMerchantCategoryBlacklist()));
            rule.setTransactionLimit(ruleCommand.getTransactionLimit());
            rule.setDailyLimit(ruleCommand.getDailyLimit());
            rule.setMonthlyLimit(ruleCommand.getMonthlyLimit());
            rule.setCoolingPeriodMinutes(ruleCommand.getCoolingPeriodMinutes());
            product.addUsageRule(rule);
        });
    }

    public void updateAccounting(OverdraftProduct product, OverdraftProductAccountingCommand accountingCommand) {
        if (accountingCommand == null) {
            product.setAccounting(null);
            return;
        }
        OverdraftProductAccounting accounting = product.getAccounting();
        if (accounting == null) {
            accounting = new OverdraftProductAccounting();
        }
        accounting.setReceivableAccount(
                glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountingCommand.getReceivableAccountId()));
        accounting.setInterestIncomeAccount(
                glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountingCommand.getInterestIncomeAccountId()));
        if (accountingCommand.getFeeIncomeAccountId() != null) {
            accounting.setFeeIncomeAccount(
                    glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountingCommand.getFeeIncomeAccountId()));
        } else {
            accounting.setFeeIncomeAccount(null);
        }
        if (accountingCommand.getSuspenseAccountId() != null) {
            accounting.setSuspenseAccount(
                    glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountingCommand.getSuspenseAccountId()));
        } else {
            accounting.setSuspenseAccount(null);
        }
        if (accountingCommand.getWriteOffAccountId() != null) {
            accounting.setWriteOffAccount(
                    glAccountRepositoryWrapper.findOneWithNotFoundDetection(accountingCommand.getWriteOffAccountId()));
        } else {
            accounting.setWriteOffAccount(null);
        }
        if (accountingCommand.getPenaltyIncomeAccountId() != null) {
            accounting.setPenaltyIncomeAccount(glAccountRepositoryWrapper
                    .findOneWithNotFoundDetection(accountingCommand.getPenaltyIncomeAccountId()));
        } else {
            accounting.setPenaltyIncomeAccount(null);
        }
        accounting
                .setAccrualEnabled(accountingCommand.getAccrualEnabled() != null && accountingCommand.getAccrualEnabled());
        product.setAccounting(accounting);
    }

    public void replaceVersions(OverdraftProduct product, List<OverdraftProductVersionCommand> versionCommands) {
        product.clearVersions();
        if (versionCommands == null) {
            return;
        }
        versionCommands.forEach(versionCommand -> {
            OverdraftProductVersion version = new OverdraftProductVersion();
            version.setVersionLabel(versionCommand.getVersionLabel());
            version.setEffectiveFrom(versionCommand.getEffectiveFrom());
            version.setEffectiveTo(versionCommand.getEffectiveTo());
            version.setNotes(versionCommand.getNotes());
            product.addVersion(version);
        });
    }

    private Set<String> copySet(Set<String> values) {
        if (values == null) {
            return Collections.emptySet();
        }
        return values.stream().filter(Objects::nonNull).map(String::trim).filter(s -> !s.isEmpty()).map(String::toUpperCase)
                .collect(Collectors.toSet());
    }

    private OverdraftFeeData mapFee(OverdraftFee fee) {
        return OverdraftFeeData.builder().id(fee.getId()).feeType(fee.getFeeType())
                .calculationType(fee.getCalculationType()).amount(fee.getAmount()).percentage(fee.getPercentage())
                .tierDefinition(fee.getTierDefinition()).triggerCondition(fee.getTriggerCondition())
                .active(fee.isActive()).build();
    }

    private OverdraftUsageRuleData mapUsageRule(OverdraftUsageRule rule) {
        return OverdraftUsageRuleData.builder().id(rule.getId()).channel(rule.getChannel()).allowed(rule.isAllowed())
                .merchantCategoryWhitelist(rule.getMerchantCategoryWhitelist())
                .merchantCategoryBlacklist(rule.getMerchantCategoryBlacklist()).transactionLimit(rule.getTransactionLimit())
                .dailyLimit(rule.getDailyLimit()).monthlyLimit(rule.getMonthlyLimit())
                .coolingPeriodMinutes(rule.getCoolingPeriodMinutes()).build();
    }

    private OverdraftProductAccountingData mapAccounting(OverdraftProductAccounting accounting) {
        if (accounting == null) {
            return null;
        }
        return OverdraftProductAccountingData.builder()
                .receivableAccountId(accounting.getReceivableAccount() != null ? accounting.getReceivableAccount().getId() : null)
                .interestIncomeAccountId(
                        accounting.getInterestIncomeAccount() != null ? accounting.getInterestIncomeAccount().getId() : null)
                .feeIncomeAccountId(
                        accounting.getFeeIncomeAccount() != null ? accounting.getFeeIncomeAccount().getId() : null)
                .suspenseAccountId(accounting.getSuspenseAccount() != null ? accounting.getSuspenseAccount().getId() : null)
                .writeOffAccountId(accounting.getWriteOffAccount() != null ? accounting.getWriteOffAccount().getId() : null)
                .penaltyIncomeAccountId(
                        accounting.getPenaltyIncomeAccount() != null ? accounting.getPenaltyIncomeAccount().getId() : null)
                .accrualEnabled(accounting.isAccrualEnabled()).build();
    }

    private OverdraftProductVersionData mapVersion(OverdraftProductVersion version) {
        return OverdraftProductVersionData.builder().id(version.getId()).versionLabel(version.getVersionLabel())
                .effectiveFrom(version.getEffectiveFrom()).effectiveTo(version.getEffectiveTo()).notes(version.getNotes())
                .build();
    }
}
