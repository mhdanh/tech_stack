package com.mhdanh.techstack.websocket;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ProxyWebSocketHandler extends TextWebSocketHandler {

    private WebSocketSession agentSession;
    private Map<String, CompletableFuture<String>> pendingRequests = new ConcurrentHashMap<>();

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message)
            throws IOException {
        String payload = message.getPayload();
        System.out.println("Received: " + payload);
        String[] msgResponse = payload.split("::");
        String responseId = msgResponse[0];
        String responseBody = msgResponse[1];
        CompletableFuture<String> pendingRequest = pendingRequests.remove(responseId);
        if(pendingRequest != null) {
            pendingRequest.complete(responseBody);
        }
    }

    public void sendToAgent(String id, String payload) throws IOException {
        String message = id + "::" + payload;
        if(agentSession != null && agentSession.isOpen()) {
            agentSession.sendMessage(new TextMessage(message));
        } else {
            throw new IllegalStateException("Client is not connected");
        }
    }

    public void registerPendingRequest(String id, CompletableFuture<String> future) {
        pendingRequests.put(id, future);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        this.agentSession = session;
    }

}