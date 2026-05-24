package RUT.PlanningFlow.adapter.out.common;

import org.slf4j.Logger;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class OutboundCallRetry {

    private OutboundCallRetry() {
    }

    public static <T> T executeWithRetry(
            final Supplier<T> action,
            final int maxAttempts,
            final String operation,
            final Logger log
    ) {
        return executeWithRetry(action, ignored -> false, maxAttempts, operation, log);
    }

    public static <T> T executeWithRetry(
            final Supplier<T> action,
            final Predicate<T> retryOnResult,
            final int maxAttempts,
            final String operation,
            final Logger log
    ) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                final T result = action.get();
                if (!retryOnResult.test(result)) {
                    return result;
                }
                if (attempt >= maxAttempts) {
                    return result;
                }
                log.warn(
                        "{}: попытка {}/{} не удалась ({}), повтор",
                        operation,
                        attempt,
                        maxAttempts,
                        describeResult(result)
                );
            } catch (final Exception e) {
                lastException = e;
                if (!isRetryable(e) || attempt >= maxAttempts) {
                    break;
                }
                log.warn(
                        "{}: попытка {}/{} не удалась ({}), повтор",
                        operation,
                        attempt,
                        maxAttempts,
                        e.getMessage()
                );
            }
        }
        throw new IllegalStateException(operation + ": " + (lastException != null ? lastException.getMessage() : "unknown"), lastException);
    }

    public static boolean isRetryable(final Exception exception) {
        if (exception instanceof ResourceAccessException) {
            return true;
        }
        if (exception instanceof HttpServerErrorException) {
            return true;
        }
        if (exception instanceof RestClientResponseException responseException
                && responseException.getStatusCode().is5xxServerError()) {
            return true;
        }
        Throwable current = exception;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof ConnectException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    public static RestTemplate createRestTemplate(final int connectTimeoutMs, final int readTimeoutMs) {
        final SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeoutMs);
        factory.setReadTimeout(readTimeoutMs);
        return new RestTemplate(factory);
    }

    private static String describeResult(final Object result) {
        return result != null ? result.toString() : "null";
    }
}
