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

import com.yourorg.fineract.plugins.overdraftproduct.serialization.OverdraftProductCommand;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.data.ApiParameterError;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.exception.PlatformApiDataValidationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OverdraftProductValidationService {

    public void validateForCreate(OverdraftProductCommand command) {
        List<ApiParameterError> dataValidationErrors = new ArrayList<>();
        DataValidatorBuilder builder = new DataValidatorBuilder(dataValidationErrors).resource("overdraftProduct");

        validateLimits(command, dataValidationErrors, builder);
        validateEffectiveDates(command, dataValidationErrors, builder);
        validateVersionLabel(command, dataValidationErrors, builder);
        validateUsageRules(command, dataValidationErrors, builder);

        if (command.getAccounting() != null) {
            builder.reset().parameter("accounting.loanPortfolioAccountId").value(command.getAccounting().getLoanPortfolioAccountId())
                    .notNull();
            builder.reset().parameter("accounting.interestIncomeAccountId")
                    .value(command.getAccounting().getInterestIncomeAccountId()).notNull();
        }

        if (!dataValidationErrors.isEmpty()) {
            log.debug("Overdraft product validation failed: {}", dataValidationErrors);
            throw new PlatformApiDataValidationException(dataValidationErrors);
        }
    }

    private void validateLimits(OverdraftProductCommand command, List<ApiParameterError> dataValidationErrors,
            DataValidatorBuilder builder) {
        BigDecimal min = command.getMinimumLimit();
        BigDecimal max = command.getMaximumLimit();
        BigDecimal defaultLimit = command.getDefaultLimit();

        if (min != null && max != null && min.compareTo(max) > 0) {
            dataValidationErrors
                    .add(ApiParameterError.parameterError("validation.msg.overdraft.limit.invalid.range",
                            "Minimum limit must be less than or equal to maximum limit", "minimumLimit", min, max));
        }

        if (defaultLimit != null) {
            if (min != null && defaultLimit.compareTo(min) < 0) {
                dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.overdraft.limit.default.out.of.range",
                        "Default limit must be greater than minimum limit", "defaultLimit", defaultLimit));
            }
            if (max != null && defaultLimit.compareTo(max) > 0) {
                dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.overdraft.limit.default.out.of.range",
                        "Default limit must be less than maximum limit", "defaultLimit", defaultLimit));
            }
        }

        builder.reset().parameter("minimumLimit").value(min).notLessThanZero();
        builder.reset().parameter("maximumLimit").value(max).notLessThanZero();
        builder.reset().parameter("defaultLimit").value(defaultLimit).notLessThanZero();
    }

    private void validateEffectiveDates(OverdraftProductCommand command, List<ApiParameterError> dataValidationErrors,
            DataValidatorBuilder builder) {
        builder.reset().parameter("effectiveFrom").value(command.getEffectiveFrom()).notNull();
        if (command.getEffectiveFrom() != null && command.getEffectiveUntil() != null
                && command.getEffectiveUntil().isBefore(command.getEffectiveFrom())) {
            dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.overdraft.invalid.effective.dates",
                    "Effective until must be after effective from", "effectiveUntil", command.getEffectiveUntil()));
        }
    }

    private void validateVersionLabel(OverdraftProductCommand command, List<ApiParameterError> dataValidationErrors,
            DataValidatorBuilder builder) {
        builder.reset().parameter("versionLabel").value(command.getVersionLabel()).notBlank();
        if (StringUtils.isBlank(command.getVersionLabel())) {
            dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.overdraft.version.label.required",
                    "Version label is required", "versionLabel"));
        }
    }

    private void validateUsageRules(OverdraftProductCommand command, List<ApiParameterError> dataValidationErrors,
            DataValidatorBuilder builder) {
        Set<String> channels = new HashSet<>();
        command.getUsageRules().forEach(rule -> {
            builder.reset().parameter("usageRules.channel").value(rule.getChannel()).notBlank();
            if (!channels.add(rule.getChannel().toLowerCase())) {
                dataValidationErrors.add(ApiParameterError.parameterError("validation.msg.overdraft.channel.duplicate",
                        "Usage rule channel duplicated", "usageRules.channel", rule.getChannel()));
            }
        });
    }
}
