package com.sakura.runtime;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket configuration for COBOL screen programs. Activated only when screen.renderer=websocket.
 * Maps /ws/{programId} to the screen WebSocket handler.
 */
@Configuration
@EnableWebSocket
@ConditionalOnProperty(name = "screen.renderer", havingValue = "websocket")
public class WebSocketScreenConfig implements WebSocketConfigurer {

    private final WebSocketScreenHandler handler;

    public WebSocketScreenConfig(WebSocketScreenHandler handler) {
        this.handler = handler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/**").setAllowedOrigins("*");
    }
}
