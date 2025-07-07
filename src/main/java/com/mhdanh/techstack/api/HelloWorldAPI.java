package com.mhdanh.techstack.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hello-world")
public class HelloWorldAPI {

    @GetMapping
    public String hayHi() {
        return "hello world!";
    }

}
