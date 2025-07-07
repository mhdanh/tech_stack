package com.mhdanh.techstack.config;

import java.net.URI;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.mhdanh.techstack.dto.ProxyRequest;
import com.mhdanh.techstack.dto.ProxyResponse;
import com.mhdanh.techstack.websocket.client.AgentWebSocketClient;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.WebSocketContainer;

@Configuration
public class AppConfig {

//    @Bean
    public CommandLineRunner clientAppAgent() {
        return args -> {
            RestTemplate restTemplate = new RestTemplate();
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                System.out.println("Start call check next request");
                ResponseEntity<ProxyRequest> nextRequest = restTemplate.getForEntity("http://localhost:8080/api/proxy/next-request/deferred", ProxyRequest.class);
                if(nextRequest.getStatusCode().is2xxSuccessful() && nextRequest.getBody() != null) {
                    ProxyRequest request = nextRequest.getBody();
                    System.out.println("Received request: " + request);
                    // Simulate processing the request and sending a response
                    String newBody = request.getBody() + request.getPath();
                    restTemplate.postForEntity("http://localhost:8080/api/proxy/respond",
                                               new ProxyResponse(request.getId(), newBody), String.class);
                }
            }, 0, 2, TimeUnit.SECONDS);
        };
    }

    @Bean
    public CommandLineRunner connectViaWebSocket() {
        return args -> {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            URI uri = URI.create("ws://localhost:8080/ws/proxy?userId=client"); // Change to your server's public IP if remote

            while (true) {
                try {
                    container.connectToServer(AgentWebSocketClient.class, uri);
                    break;
                } catch (Exception e) {
                    System.out.println("Failed to connect, retrying in 3s...");
                    Thread.sleep(3000);
                }
            }
        };
    }
}
