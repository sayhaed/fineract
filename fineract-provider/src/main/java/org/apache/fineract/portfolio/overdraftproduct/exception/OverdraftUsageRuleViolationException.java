package org.apache.fineract.portfolio.overdraftproduct.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OverdraftUsageRuleViolationException extends AbstractPlatformDomainRuleException {

    public OverdraftUsageRuleViolationException(String errorCode, String defaultMessage, Object... defaultArgs) {
        super("error.msg.overdraftproduct.usage." + errorCode, defaultMessage, defaultArgs);
    }
}
