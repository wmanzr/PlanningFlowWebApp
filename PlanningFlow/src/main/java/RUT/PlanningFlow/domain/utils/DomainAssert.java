package RUT.PlanningFlow.domain.utils;

import RUT.PlanningFlow.domain.exception.DomainException;

public final class DomainAssert {
    private DomainAssert() {
    }

    public static void notNull(final Object object, final String message, final String errorCode) {
        if (object == null) {
            throw new DomainException(message, errorCode);
        }
    }

    public static void notBlank(final String value, final String message, final String errorCode) {
        if (value == null || value.isBlank()) {
            throw new DomainException(message, errorCode);
        }
    }

    public static void isTrue(final boolean condition, final String message, final String errorCode) {
        if (!condition) {
            throw new DomainException(message, errorCode);
        }
    }
}