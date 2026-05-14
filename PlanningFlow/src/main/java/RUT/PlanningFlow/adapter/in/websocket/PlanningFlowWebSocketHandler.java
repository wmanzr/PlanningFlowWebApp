package RUT.PlanningFlow.adapter.in.websocket;

import RUT.PlanningFlow.adapter.out.notification.WebSocketSessionRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class PlanningFlowWebSocketHandler extends TextWebSocketHandler {
    private static final Logger log = LoggerFactory.getLogger(PlanningFlowWebSocketHandler.class);

    private final WebSocketSessionRegistry registry;

    public PlanningFlowWebSocketHandler(final WebSocketSessionRegistry registry) {
        this.registry = registry;
    }

    @Override
    public void afterConnectionEstablished(final WebSocketSession session) throws Exception {
        final Object uid = session.getAttributes().get(WsSessionAttributes.USER_ID);
        if (!(uid instanceof Integer userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Unauthorized"));
            return;
        }
        registry.register(userId, session);
        log.info("WebSocket connected userId={} sessionId={}", userId, session.getId());
    }

    @Override
    public void afterConnectionClosed(final WebSocketSession session, final CloseStatus status) {
        registry.unregister(session);
        log.info("WebSocket disconnected sessionId={} ({})", session.getId(), status);
    }
}