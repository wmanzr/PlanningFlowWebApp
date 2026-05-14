package RUT.PlanningFlow.adapter.out.notification;

import RUT.PlanningFlow.adapter.in.websocket.WsSessionAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebSocketSessionRegistry {

    private final ConcurrentHashMap<Integer, CopyOnWriteArraySet<WebSocketSession>> sessionsByUserId =
            new ConcurrentHashMap<>();

    public void register(final int userId, final WebSocketSession session) {
        if (session == null || userId <= 0) {
            return;
        }
        sessionsByUserId.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>()).add(session);
    }

    public void unregister(final WebSocketSession session) {
        if (session == null) {
            return;
        }
        final Object raw = session.getAttributes().get(WsSessionAttributes.USER_ID);
        if (!(raw instanceof Integer userId)) {
            return;
        }
        final CopyOnWriteArraySet<WebSocketSession> set = sessionsByUserId.get(userId);
        if (set == null) {
            return;
        }
        set.remove(session);
        if (set.isEmpty()) {
            sessionsByUserId.remove(userId, set);
        }
    }

    public List<WebSocketSession> sessionsForUser(final int userId) {
        final CopyOnWriteArraySet<WebSocketSession> set = sessionsByUserId.get(userId);
        if (set == null || set.isEmpty()) {
            return List.of();
        }
        return new ArrayList<>(set);
    }
}