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
        long tokenValidMillis
) {
    public GigaChatProperties {
        if (oauthUrl == null || oauthUrl.isBlank()) {
            oauthUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
        }
        if (chatCompletionsUrl == null || chatCompletionsUrl.isBlank()) {
            chatCompletionsUrl = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
        }
        if (model == null || model.isBlank()) {
            model = "GigaChat";
        }
        if (tokenValidMillis <= 0) {
            tokenValidMillis = 25 * 60 * 1000L;
        }
    }
}