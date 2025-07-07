package com.mhdanh.techstack.api;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mhdanh.techstack.websocket.HelloWebSocketHandler;
import com.mhdanh.techstack.websocket.ProxyWebSocketHandler;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketAPI {

    private final HelloWebSocketHandler helloWebSocketHandler;
    private final ProxyWebSocketHandler proxyWebSocketHandler;


    @GetMapping("broadcast")
    public void sendMessageToALl(@RequestParam  String message, @RequestParam(required = false) String userId) throws IOException {
        helloWebSocketHandler.broadcast(userId, message);
    }

    @PostMapping("/proxy")
    public ResponseEntity<String> proxy(@RequestBody String body,
                                        @RequestParam(defaultValue = "GET") String method,
                                        @RequestParam(defaultValue = "/") String path) {
        String id = UUID.randomUUID().toString();
        String combinedRequest = method + " " + path + "\n" + body;

        CompletableFuture<String> completeResponse = new CompletableFuture<>();
        proxyWebSocketHandler.registerPendingRequest(id, completeResponse);

        try {
            proxyWebSocketHandler.sendToAgent(id, combinedRequest);
            String s = completeResponse.get(10, TimeUnit.SECONDS);// wait 10second for response
            return ResponseEntity.ok(s);
        } catch (Exception e) {
            return ResponseEntity.status(504).body("Timeout waiting for agent.");
        }
    }
}
