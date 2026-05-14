package RUT.PlanningFlow.adapter.out.notification;

import RUT.PlanningFlow.application.dto.notification.RealtimeMessage;
import RUT.PlanningFlow.application.port.out.NotificationPublisherPort;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class WebSocketNotificationPublisherAdapter implements NotificationPublisherPort {

    private static final Logger log = LoggerFactory.getLogger(WebSocketNotificationPublisherAdapter.class);

    private final WebSocketSessionRegistry registry;
    private final Gson gson;

    public WebSocketNotificationPublisherAdapter(final WebSocketSessionRegistry registry, final Gson gson) {
        this.registry = registry;
        this.gson = gson;
    }

    @Override
    public void publishToUser(final int userId, final RealtimeMessage message) {
        if (message == null) {
            return;
        }
        send(registry.sessionsForUser(userId), envelopeJson(message));
    }

    @Override
    public void publishToUsers(final Collection<Integer> userIds, final RealtimeMessage message) {
        if (message == null || userIds == null || userIds.isEmpty()) {
            return;
        }
        final Set<Integer> distinct = new LinkedHashSet<>();
        for (final Integer id : userIds) {
            if (id != null && id > 0) {
                distinct.add(id);
            }
        }
        final String json = envelopeJson(message);
        for (final int userId : distinct) {
            send(registry.sessionsForUser(userId), json);
        }
    }

    private void send(final List<WebSocketSession> sessions, final String json) {
        if (sessions.isEmpty()) {
            return;
        }
        for (final WebSocketSession session : sessions) {
            if (session == null || !session.isOpen()) {
                continue;
            }
            try {
                synchronized (session) {
                    session.sendMessage(new TextMessage(json));
                }
            } catch (final IOException e) {
                log.warn("WebSocket send failed session {}", session.getId(), e);
            }
        }
    }

    private String envelopeJson(final RealtimeMessage message) {
        final JsonObject root = new JsonObject();
        root.addProperty("type", message.type());
        root.addProperty("timestamp", message.timestamp());
        final String rawPayload = message.payloadJson();
        if (rawPayload == null || rawPayload.isBlank()) {
            root.add("payload", JsonParser.parseString("{}"));
        } else {
            root.add("payload", JsonParser.parseString(rawPayload));
        }
        return gson.toJson(root);
    }
}