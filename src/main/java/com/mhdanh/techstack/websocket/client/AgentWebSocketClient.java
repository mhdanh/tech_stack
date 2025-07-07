package com.mhdanh.techstack.websocket.client;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.CloseReason;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;

@ClientEndpoint
public class AgentWebSocketClient {

    private Session session;

    @OnOpen
    public void onOpen(Session session) {
        System.out.println("Connected to server");
        this.session = session;
    }

    @OnMessage
    public void onMessage(String message) {
        // Message format: requestId::method path\nbody
        String[] parts = message.split("::", 2);
        if (parts.length < 2) return;

        String requestId = parts[0];
        String payload = parts[1];

        // 👇 Simulate or forward the request locally
        String result = handleRequest(payload);

        // 👈 Send back to server
        try {
            session.getAsyncRemote().sendText(requestId + "::" + result);
        } catch (Exception e) {
            System.err.println("Failed to respond: " + e.getMessage());
        }
    }

    private String handleRequest(String body) {
        return "Process by websocket: " + body;
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        System.err.println("WebSocket error: " + throwable.getMessage());
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        System.out.println("Connection closed: " + reason);
    }
}
