package org.apache.fineract.portfolio.overdraftproduct.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftFeeCalculationType;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProduct;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftProductVersion;
import org.apache.fineract.portfolio.overdraftproduct.domain.OverdraftUsageRule;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftProductLifecycleException;
import org.apache.fineract.portfolio.overdraftproduct.exception.OverdraftUsageRuleViolationException;
import org.apache.fineract.portfolio.overdraftproduct.repository.OverdraftProductRepository;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftFeeCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductAccountingCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductLifecycleCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductValidationResult;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftProductVersionCommand;
import org.apache.fineract.portfolio.overdraftproduct.serialization.OverdraftUsageRuleCommand;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OverdraftProductValidationService {

    private static final String RESOURCE_NAME = "overdraftproduct";
    private final OverdraftProductRepository productRepository;

    public void validateForCreate(OverdraftProductCommand command) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateCommon(command, errors);
        if (productRepository.existsByNameIgnoreCase(command.getName())) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.name.duplicate",
                    "An overdraft product with the same name already exists", "name", command.getName()));
        }
        throwIfValidationErrors(errors);
    }

    public void validateForUpdate(OverdraftProduct existing, OverdraftProductCommand command) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateCommon(command, errors);
        if (!StringUtils.equalsIgnoreCase(existing.getName(), command.getName())
                && productRepository.existsByNameIgnoreCase(command.getName())) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.name.duplicate",
                    "An overdraft product with the same name already exists", "name", command.getName()));
        }
        throwIfValidationErrors(errors);
    }

    public void validateActivation(OverdraftProduct product, OverdraftProductLifecycleCommand command) {
        if (!product.getStatus().isDraft()) {
            throw new OverdraftProductLifecycleException("activation.invalid.status",
                    "Only draft overdraft products can be activated", product.getStatus());
        }
        LocalDate activationDate = Optional.ofNullable(command).map(OverdraftProductLifecycleCommand::getActionDate)
                .orElse(DateUtils.getBusinessLocalDate());
        List<ApiParameterError> errors = new ArrayList<>();
        validateVersionRanges(product.getVersions(), errors);
        if (product.getVersions().stream().noneMatch(version -> version.isEffectiveOn(activationDate))) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.not.covering.activation",
                    "No overdraft product version covers the requested activation date", "activationDate", activationDate));
        }
        throwIfValidationErrors(errors);
    }

    public void validateRetirement(OverdraftProduct product, OverdraftProductLifecycleCommand command) {
        if (!product.getStatus().isActive()) {
            throw new OverdraftProductLifecycleException("retirement.invalid.status",
                    "Only active overdraft products can be retired", product.getStatus());
        }
        LocalDate retirementDate = Optional.ofNullable(command).map(OverdraftProductLifecycleCommand::getActionDate)
                .orElse(DateUtils.getBusinessLocalDate());
        if (product.getActivatedOn() != null && retirementDate.isBefore(product.getActivatedOn())) {
            throw new OverdraftProductLifecycleException("retirement.before.activation",
                    "Retirement date cannot be before activation date", retirementDate, product.getActivatedOn());
        }
    }

    public OverdraftProductValidationResult evaluate(OverdraftProduct product) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateVersionRanges(product.getVersions(), errors);
        return OverdraftProductValidationResult.builder().valid(errors.isEmpty()).errors(errors).build();
    }

    public void validateFeesUpdate(List<OverdraftFeeCommand> fees) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateFees(fees, errors);
        throwIfValidationErrors(errors);
    }

    public void validateUsageRulesUpdate(List<OverdraftUsageRuleCommand> rules) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateUsageRules(rules, errors);
        throwIfValidationErrors(errors);
    }

    public void validateAccountingUpdate(OverdraftProductAccountingCommand accounting) {
        List<ApiParameterError> errors = new ArrayList<>();
        validateAccounting(accounting, errors);
        throwIfValidationErrors(errors);
    }

    public void validateUsage(OverdraftProduct product, OverdraftUsageValidationContext context) {
        if (context == null || product == null) {
            return;
        }
        String channel = Optional.ofNullable(context.getChannel()).orElse("ALL").toUpperCase(Locale.ROOT);
        List<OverdraftUsageRule> matchingRules = product.getUsageRules().stream()
                .filter(rule -> channelMatches(rule.getChannel(), channel)).collect(Collectors.toList());
        if (matchingRules.isEmpty()) {
            return;
        }
        matchingRules.forEach(rule -> applyUsageRule(rule, context));
    }

    private void validateCommon(OverdraftProductCommand command, List<ApiParameterError> errors) {
        DataValidatorBuilder base = new DataValidatorBuilder(errors).resource(RESOURCE_NAME);
        base.reset().parameter("name").value(command.getName()).notBlank();
        base.reset().parameter("currencyCode").value(command.getCurrencyCode()).notBlank();
        base.reset().parameter("defaultLimit").value(command.getDefaultLimit()).notNull().positiveAmount();
        base.reset().parameter("minimumLimit").value(command.getMinimumLimit()).notNull().positiveAmount();
        base.reset().parameter("maximumLimit").value(command.getMaximumLimit()).notNull().positiveAmount();
        base.reset().parameter("interestRate").value(command.getInterestRate()).notNull().positiveAmount();
        base.reset().parameter("roundingScale").value(command.getRoundingScale()).ignoreIfNull().integerZeroOrGreater();

        if (command.getMinimumLimit() != null && command.getMaximumLimit() != null
                && command.getMinimumLimit().compareTo(command.getMaximumLimit()) > 0) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.min.limit.exceeds.max",
                    "Minimum limit cannot exceed maximum limit", "minimumLimit", command.getMinimumLimit(),
                    command.getMaximumLimit()));
        }
        if (command.getDefaultLimit() != null) {
            if (command.getMinimumLimit() != null && command.getDefaultLimit().compareTo(command.getMinimumLimit()) < 0) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.default.limit.less.than.min",
                        "Default limit cannot be less than minimum limit", "defaultLimit", command.getDefaultLimit(),
                        command.getMinimumLimit()));
            }
            if (command.getMaximumLimit() != null && command.getDefaultLimit().compareTo(command.getMaximumLimit()) > 0) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.default.limit.greater.than.max",
                        "Default limit cannot exceed maximum limit", "defaultLimit", command.getDefaultLimit(),
                        command.getMaximumLimit()));
            }
        }
        validateFees(command.getFees(), errors);
        validateUsageRules(command.getUsageRules(), errors);
        validateAccounting(command.getAccounting(), errors);
        validateVersionCommands(command.getVersions(), errors);
    }

    private void validateFees(List<OverdraftFeeCommand> feeCommands, List<ApiParameterError> errors) {
        if (feeCommands == null) {
            return;
        }
        for (int i = 0; i < feeCommands.size(); i++) {
            OverdraftFeeCommand command = feeCommands.get(i);
            DataValidatorBuilder feeValidator = new DataValidatorBuilder(errors).resource(RESOURCE_NAME)
                    .parameter("fees").parameterAtIndexArray("fee", i).value(command.getFeeType());
            feeValidator.notNull();
            if (command.getCalculationType() == OverdraftFeeCalculationType.FLAT
                    && command.getAmount() == null) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.fee.amount.required",
                        "Flat fees must define an amount", "fees", i));
            }
            if (command.getCalculationType() == OverdraftFeeCalculationType.PERCENTAGE
                    && command.getPercentage() == null) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.fee.percentage.required",
                        "Percentage fees must define a percentage", "fees", i));
            }
        }
    }

    private void validateUsageRules(List<OverdraftUsageRuleCommand> ruleCommands, List<ApiParameterError> errors) {
        if (ruleCommands == null) {
            return;
        }
        DataValidatorBuilder usageValidator = new DataValidatorBuilder(errors).resource(RESOURCE_NAME);
        for (int i = 0; i < ruleCommands.size(); i++) {
            OverdraftUsageRuleCommand command = ruleCommands.get(i);
            usageValidator.reset().parameter("usageRules").parameterAtIndexArray("rule", i).value(command.getChannel()).notBlank();
            usageValidator.reset().parameter("usageRules").parameterAtIndexArray("rule", i).value(command.getTransactionLimit())
                    .ignoreIfNull().positiveAmount();
            usageValidator.reset().parameter("usageRules").parameterAtIndexArray("rule", i).value(command.getDailyLimit())
                    .ignoreIfNull().positiveAmount();
            usageValidator.reset().parameter("usageRules").parameterAtIndexArray("rule", i).value(command.getMonthlyLimit())
                    .ignoreIfNull().positiveAmount();
        }
    }

    private void validateAccounting(OverdraftProductAccountingCommand accountingCommand, List<ApiParameterError> errors) {
        DataValidatorBuilder accountingValidator = new DataValidatorBuilder(errors).resource(RESOURCE_NAME);
        accountingValidator.reset().parameter("accounting").value(accountingCommand).notNull();
        if (accountingCommand != null) {
            accountingValidator.reset().parameter("accounting.receivableAccountId")
                    .value(accountingCommand.getReceivableAccountId()).notNull();
            accountingValidator.reset().parameter("accounting.interestIncomeAccountId")
                    .value(accountingCommand.getInterestIncomeAccountId()).notNull();
        }
    }

    private void validateVersionCommands(List<OverdraftProductVersionCommand> versionCommands, List<ApiParameterError> errors) {
        if (versionCommands == null || versionCommands.isEmpty()) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.required",
                    "At least one version must be defined", "versions"));
            return;
        }
        List<VersionRange> ranges = versionCommands.stream()
                .map(cmd -> new VersionRange(cmd.getVersionLabel(), cmd.getEffectiveFrom(), cmd.getEffectiveTo()))
                .collect(Collectors.toList());
        validateRanges(ranges, errors);
    }

    private void validateVersionRanges(List<OverdraftProductVersion> versions, List<ApiParameterError> errors) {
        if (versions == null || versions.isEmpty()) {
            errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.required",
                    "At least one version must be defined", "versions"));
            return;
        }
        List<VersionRange> ranges = versions.stream()
                .map(version -> new VersionRange(version.getVersionLabel(), version.getEffectiveFrom(), version.getEffectiveTo()))
                .collect(Collectors.toList());
        validateRanges(ranges, errors);
    }

    private void validateRanges(List<VersionRange> ranges, List<ApiParameterError> errors) {
        ranges.sort(Comparator.comparing(VersionRange::effectiveFrom, Comparator.nullsFirst(Comparator.naturalOrder())));
        for (int i = 0; i < ranges.size(); i++) {
            VersionRange range = ranges.get(i);
            if (range.effectiveFrom == null) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.from.required",
                        "Version effective from date is required", "versions", i));
            }
            if (range.effectiveFrom != null && range.effectiveTo != null
                    && range.effectiveTo.isBefore(range.effectiveFrom)) {
                errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.invalid.range",
                        "Version effective to date cannot be before effective from date", "versions", range.label));
            }
            if (i > 0) {
                VersionRange previous = ranges.get(i - 1);
                LocalDate previousEnd = Optional.ofNullable(previous.effectiveTo).orElse(LocalDate.MAX);
                if (previousEnd.isAfter(range.effectiveFrom)) {
                    errors.add(ApiParameterError.parameterError("validation.msg.overdraftproduct.version.overlap",
                            "Overdraft product versions cannot overlap", "versions", range.label, previous.label));
                }
            }
        }
    }

    private void throwIfValidationErrors(List<ApiParameterError> errors) {
        if (!errors.isEmpty()) {
            throw new PlatformApiDataValidationException(errors);
        }
    }

    private boolean channelMatches(String ruleChannel, String requestedChannel) {
        if (StringUtils.isBlank(ruleChannel)) {
            return true;
        }
        if ("ALL".equalsIgnoreCase(ruleChannel)) {
            return true;
        }
        return ruleChannel.equalsIgnoreCase(requestedChannel);
    }

    private void applyUsageRule(OverdraftUsageRule rule, OverdraftUsageValidationContext context) {
        if (!rule.isAllowed()) {
            throw new OverdraftUsageRuleViolationException("channel.blocked",
                    String.format("Transactions on channel %s are not allowed for this overdraft product", context.getChannel()),
                    context.getChannel());
        }
        String mcc = Optional.ofNullable(context.getMerchantCategoryCode()).map(code -> code.toUpperCase(Locale.ROOT)).orElse(null);
        if (StringUtils.isNotBlank(mcc)) {
            if (!rule.getMerchantCategoryWhitelist().isEmpty() && !rule.getMerchantCategoryWhitelist().contains(mcc)) {
                throw new OverdraftUsageRuleViolationException("mcc.not.whitelisted",
                        String.format("Merchant category %s is not whitelisted for channel %s", mcc, context.getChannel()),
                        mcc, context.getChannel());
            }
            if (rule.getMerchantCategoryBlacklist().contains(mcc)) {
                throw new OverdraftUsageRuleViolationException("mcc.blacklisted",
                        String.format("Merchant category %s is blacklisted for channel %s", mcc, context.getChannel()),
                        mcc, context.getChannel());
            }
        }
        validateLimit(rule.getTransactionLimit(), context.getTransactionAmount(), "transactionLimit", context.getChannel());
        validateLimit(rule.getDailyLimit(), context.getDayToDateUsage(), "dailyLimit", context.getChannel());
        validateLimit(rule.getMonthlyLimit(), context.getMonthToDateUsage(), "monthlyLimit", context.getChannel());
    }

    private void validateLimit(BigDecimal limit, BigDecimal value, String limitType, String channel) {
        if (limit != null && value != null && value.compareTo(limit) > 0) {
            throw new OverdraftUsageRuleViolationException(limitType + ".exceeded",
                    String.format("%s exceeded for channel %s", StringUtils.capitalize(limitType), channel), channel, limit);
        }
    }

    private record VersionRange(String label, LocalDate effectiveFrom, LocalDate effectiveTo) {
    }
}
