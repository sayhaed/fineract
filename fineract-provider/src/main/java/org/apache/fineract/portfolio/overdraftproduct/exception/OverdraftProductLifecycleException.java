package org.apache.fineract.portfolio.overdraftproduct.exception;

import org.apache.fineract.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class OverdraftProductLifecycleException extends AbstractPlatformDomainRuleException {

    public OverdraftProductLifecycleException(String errorCode, String defaultMessage, Object... defaultArgs) {
        super("error.msg.overdraftproduct.lifecycle." + errorCode, defaultMessage, defaultArgs);
    }
}
