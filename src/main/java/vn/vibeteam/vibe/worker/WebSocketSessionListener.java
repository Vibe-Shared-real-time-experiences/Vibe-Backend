package vn.vibeteam.vibe.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@Slf4j
public class WebSocketEventListener {

    @EventListener
    public void handleSubscribeEvent(SessionSubscribeEvent event) {
        log.info("📢 NEW SUBSCRIPTION DETECTED: " + event.getMessage());
    }

    @EventListener
    public void handleConnectEvent(SessionConnectedEvent event) {
        log.info("🔌 NEW CONNECTION: " + event.getUser());
    }
}