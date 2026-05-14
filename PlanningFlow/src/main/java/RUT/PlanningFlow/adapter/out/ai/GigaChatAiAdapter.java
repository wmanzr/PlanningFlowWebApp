package RUT.PlanningFlow.adapter.out.ai;

import RUT.PlanningFlow.application.port.out.AIPort;
import RUT.PlanningFlow.config.ai.GigaChatProperties;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Component
public class GigaChatAiAdapter implements AIPort {

    private final GigaChatProperties properties;
    private final Gson gson;
    private final RestTemplate http;

    private String accessToken;
    private long accessTokenExpiresAtMillis;

    public GigaChatAiAdapter(final GigaChatProperties properties, final Gson gson) {
        this.properties = properties;
        this.gson = gson;
        if (properties.trustAllSsl()) {
            InsecureSsl.applyOnceForJvm();
        }
        this.http = new RestTemplate();
    }

    @Override
    public String complete(final String systemPrompt, final String userContent) {
        if (properties.auth() == null || properties.auth().isBlank()) {
            throw new IllegalStateException("Задайте gigachat.auth или переменную окружения GIGACHAT_AUTH");
        }
        if (systemPrompt == null || systemPrompt.isBlank()) {
            throw new IllegalArgumentException("systemPrompt обязателен");
        }
        if (userContent == null || userContent.isBlank()) {
            throw new IllegalArgumentException("userContent обязателен");
        }

        ensureAccessToken();

        final List<ChatMessageJson> messages = List.of(
                new ChatMessageJson("system", systemPrompt),
                new ChatMessageJson("user", userContent)
        );
        final ChatRequestJson body = new ChatRequestJson(properties.model(), messages, properties.temperature());

        final HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        try {
            final String json = gson.toJson(body);
            final HttpEntity<String> entity = new HttpEntity<>(json, headers);
            final ResponseEntity<String> raw = http.postForEntity(properties.chatCompletionsUrl(), entity, String.class);
            final ChatResponseJson response = gson.fromJson(raw.getBody(), ChatResponseJson.class);
            if (response != null && response.choices != null && !response.choices.isEmpty()) {
                final ChoiceJson choice = response.choices.get(0);
                if (choice.message != null && choice.message.content != null) {
                    return choice.message.content;
                }
            }
            return "";
        } catch (final Exception e) {
            throw new IllegalStateException("GigaChat chat/completions: " + e.getMessage(), e);
        }
    }

    private synchronized void ensureAccessToken() {
        final long now = System.currentTimeMillis();
        if (accessToken != null && now < accessTokenExpiresAtMillis - 60_000L) {
            return;
        }
        refreshAccessToken();
    }

    private void refreshAccessToken() {
        final HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, basicAuthorizationHeader(properties.auth()));
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.add("RqUID", UUID.randomUUID().toString());

        final MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("scope", "GIGACHAT_API_PERS");

        try {
            final HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(form, headers);
            final ResponseEntity<String> raw = http.postForEntity(properties.oauthUrl(), request, String.class);
            final TokenResponseJson token = gson.fromJson(raw.getBody(), TokenResponseJson.class);
            if (token == null || token.accessToken == null || token.accessToken.isBlank()) {
                throw new IllegalStateException("OAuth: пустой access_token");
            }
            this.accessToken = token.accessToken;
            this.accessTokenExpiresAtMillis = System.currentTimeMillis() + properties.tokenValidMillis();
        } catch (final Exception e) {
            throw new IllegalStateException("GigaChat OAuth: " + e.getMessage(), e);
        }
    }

    private static String basicAuthorizationHeader(final String authFromConfig) {
        final String trimmed = authFromConfig.trim();
        if (trimmed.regionMatches(true, 0, "Basic ", 0, 6)) {
            return trimmed;
        }
        return "Basic " + trimmed;
    }

    private static final class ChatMessageJson {
        final String role;
        final String content;

        ChatMessageJson(final String role, final String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static final class ChatRequestJson {
        final String model;
        final List<ChatMessageJson> messages;
        final double temperature;

        ChatRequestJson(final String model, final List<ChatMessageJson> messages, final double temperature) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
        }
    }

    private static final class TokenResponseJson {
        @SerializedName("access_token")
        String accessToken;
    }

    private static final class ChatResponseJson {
        List<ChoiceJson> choices;
    }

    private static final class ChoiceJson {
        MessageJson message;
    }

    private static final class MessageJson {
        String content;
    }

    static final class InsecureSsl {
        private static volatile boolean applied;

        static void applyOnceForJvm() {
            if (applied) {
                return;
            }
            synchronized (InsecureSsl.class) {
                if (applied) {
                    return;
                }
                try {
                    final javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
                    sslContext.init(null, new javax.net.ssl.TrustManager[]{new javax.net.ssl.X509TrustManager() {
                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[0];
                        }

                        @Override
                        public void checkClientTrusted(
                                final java.security.cert.X509Certificate[] certs,
                                final String authType
                        ) {
                        }

                        @Override
                        public void checkServerTrusted(
                                final java.security.cert.X509Certificate[] certs,
                                final String authType
                        ) {
                        }
                    }}, new java.security.SecureRandom());
                    javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                    javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
                    applied = true;
                } catch (final Exception e) {
                    throw new IllegalStateException("Не удалось отключить проверку SSL", e);
                }
            }
        }
    }
}