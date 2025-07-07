package com.mhdanh.techstack.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@RestController
@RequestMapping("/api/deferred")
public class DeferredAPI {

    @GetMapping("/callme")
    public DeferredResult<String> callMe() {
        DeferredResult<String> deferredResult = new DeferredResult<>(10000L);
        new Thread(() -> {
            try {
                // Simulate a long-running task
                Thread.sleep(5000);
                deferredResult.setResult("Hello, this is a deferred response!");
            } catch (InterruptedException e) {
                deferredResult.setErrorResult("Error occurred: " + e.getMessage());
            }
        }).start();
        return deferredResult;
    }

}
