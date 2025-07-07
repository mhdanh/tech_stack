package com.mhdanh.techstack.api;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

import com.mhdanh.techstack.dto.ProxyRequest;
import com.mhdanh.techstack.dto.ProxyResponse;

@RestController
@RequestMapping("/api/proxy")
public class ServerProxyAPI {

    private Queue<ProxyRequest> pendingRequests = new ConcurrentLinkedQueue<>();
    private Map<String, CompletableFuture<String>> responseFutures = new ConcurrentHashMap<>();
    private List<DeferredResult<ProxyRequest>> longPollers = new CopyOnWriteArrayList();

    @PostMapping("/request")
    public ResponseEntity<String> proxy(@RequestBody String body,
                        @RequestParam(defaultValue = "GET") String method,
                        @RequestParam(defaultValue = "/") String path) {
        String id = UUID.randomUUID().toString();
        ProxyRequest request = new ProxyRequest(id, method, path, body);
        if(!longPollers.isEmpty()) {
            // notify all waiting result
            for (DeferredResult<ProxyRequest> longPoller : longPollers) {
                longPoller.setResult(request);
            }
            // clear the long pollers list
            longPollers.clear();
        } else {
            pendingRequests.add(request);
        }
        CompletableFuture<String> completeResponse = new CompletableFuture<>();
        responseFutures.put(id, completeResponse);

        try {
            String s = completeResponse.get(10, TimeUnit.SECONDS);// wait 10second for response
            return ResponseEntity.ok(s);
        } catch (Exception e) {
            return ResponseEntity.status(504).body("Timeout waiting for agent.");
        } finally {
            responseFutures.remove(id);
        }
    }

    @GetMapping("/next-request")
    public ResponseEntity<ProxyRequest> nextRequest() {
        ProxyRequest next = pendingRequests.poll();
        if (next == null) {
            return ResponseEntity.noContent().build(); // Không có request nào
        }
        return ResponseEntity.ok(next);
    }

    @GetMapping("/next-request/deferred")
    public DeferredResult<ProxyRequest> nextRequestLongPoller() {
        DeferredResult<ProxyRequest> deferredResult = new DeferredResult<>(10000L); // 20 seconds timeout
        ProxyRequest next = pendingRequests.poll();
        if (next != null) {
            // has request and response
            deferredResult.setResult(next);
        } else {
            // no request a the moment then wait
            longPollers.add(deferredResult);
        }
        deferredResult.onTimeout(() -> {
            deferredResult.setResult(null);
        });
        return deferredResult;
    }

    @PostMapping("/respond")
    public ResponseEntity<String> respond(@RequestBody ProxyResponse response) {
        CompletableFuture<String> future = responseFutures.get(response.getId());
        if (future != null) {
            future.complete(response.getBody());
            return ResponseEntity.ok("OK");
        }
        return ResponseEntity.badRequest().body("Request not found or timed out.");
    }
}
