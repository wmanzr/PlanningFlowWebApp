package RUT.PlanningFlow.adapter.in.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final PlanningFlowWebSocketHandler handler;
    private final JwtWebSocketHandshakeInterceptor jwtHandshakeInterceptor;
    private final String[] allowedOriginPatterns;

    public WebSocketConfig(
            final PlanningFlowWebSocketHandler handler,
            final JwtWebSocketHandshakeInterceptor jwtHandshakeInterceptor,
            @Value("${planningflow.websocket.allowed-origin-patterns:http://localhost:*,http://127.0.0.1:*}") final String patternsCsv
    ) {
        this.handler = handler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
        this.allowedOriginPatterns = mergeDefaultPatterns(splitCsv(patternsCsv));
    }

    @Override
    public void registerWebSocketHandlers(final WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/api/v1/ws/events")
                .addInterceptors(jwtHandshakeInterceptor)
                .setAllowedOriginPatterns(allowedOriginPatterns);
    }

    private static String[] mergeDefaultPatterns(final String[] fromProperty) {
        final Set<String> set = new LinkedHashSet<>();
        set.addAll(Arrays.asList("http://localhost:*", "http://127.0.0.1:*"));
        for (final String p : fromProperty) {
            if (p != null && !p.isBlank()) {
                set.add(p.trim());
            }
        }
        return set.toArray(String[]::new);
    }

    private static String[] splitCsv(final String csv) {
        if (csv == null || csv.isBlank()) {
            return new String[0];
        }
        return java.util.Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toArray(String[]::new);
    }
}