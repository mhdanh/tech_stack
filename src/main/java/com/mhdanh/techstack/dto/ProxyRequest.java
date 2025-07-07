package com.mhdanh.techstack.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyRequest {
    private String id;
    private String method;
    private String path;
    private String body;
}
