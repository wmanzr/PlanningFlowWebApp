package RUT.PlanningFlow.config.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "gigachat")
@Validated
public record GigaChatProperties(
        String auth,
        String oauthUrl,
        String chatCompletionsUrl,
        String model,
        double temperature,
        boolean trustAllSsl,
        long tokenValidMillis,
        int connectTimeoutMs,
        int readTimeoutMs,
        int maxAttempts
) {
}