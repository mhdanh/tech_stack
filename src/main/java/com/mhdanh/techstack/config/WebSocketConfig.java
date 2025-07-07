package com.mhdanh.techstack.config;

import java.util.Map;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.HandshakeInterceptor;

import com.mhdanh.techstack.websocket.HelloWebSocketHandler;
import com.mhdanh.techstack.websocket.ProxyWebSocketHandler;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final HelloWebSocketHandler helloWebSocketHandler;
    private final ProxyWebSocketHandler proxyWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(helloWebSocketHandler, "/ws/hello")
                .addInterceptors(authenticationUserInterceptor())
                .addHandler(proxyWebSocketHandler, "/ws/proxy")
                .setAllowedOrigins("*");
    }

    private HandshakeInterceptor authenticationUserInterceptor() {
        return new HandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                           WebSocketHandler wsHandler, Map<String, Object> attributes)
                    throws Exception {
                if (request instanceof ServletServerHttpRequest servletRequest) {
                    HttpServletRequest req = servletRequest.getServletRequest();
                    String userId = req.getParameter("userId"); // or JWT from header/cookie

                    if (userId != null && !userId.isBlank()) {
                        attributes.put("userId", userId);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                       WebSocketHandler wsHandler, Exception exception) {

            }
        };
    }
}
