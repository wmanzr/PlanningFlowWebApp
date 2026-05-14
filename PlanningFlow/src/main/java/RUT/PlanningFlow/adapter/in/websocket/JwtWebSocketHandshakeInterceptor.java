package RUT.PlanningFlow.adapter.in.websocket;

import RUT.PlanningFlow.adapter.in.web.security.JwtProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
public class JwtWebSocketHandshakeInterceptor implements org.springframework.web.socket.server.HandshakeInterceptor {

    private final JwtProvider jwtProvider;

    public JwtWebSocketHandshakeInterceptor(final JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    public boolean beforeHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Map<String, Object> attributes
    ) {
        final String token = extractAccessToken(request);
        if (token == null || token.isBlank()) {
            return false;
        }
        try {
            final Claims claims = jwtProvider.parseAndValidateAccess(token.trim());
            final int userId = JwtProvider.userIdFromSubject(claims);
            attributes.put(WsSessionAttributes.USER_ID, userId);
            return true;
        } catch (final JwtException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            final ServerHttpRequest request,
            final ServerHttpResponse response,
            final WebSocketHandler wsHandler,
            final Exception exception
    ) {
    }

    private static String extractAccessToken(final ServerHttpRequest request) {
        if (request instanceof ServletServerHttpRequest servlet) {
            final String auth = servlet.getServletRequest().getHeader(HttpHeaders.AUTHORIZATION);
            if (auth != null && auth.regionMatches(true, 0, "Bearer ", 0, 7)) {
                return auth.substring(7).trim();
            }
        }
        final var params = UriComponentsBuilder.fromUri(request.getURI()).build().getQueryParams();
        String t = params.getFirst("token");
        if (t != null && !t.isBlank()) {
            return t.trim();
        }
        t = params.getFirst("access_token");
        return t != null ? t.trim() : null;
    }
}