package com.example.distributedcache.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class InstanceController {

    @Value("${instance.id}")
    private String instanceId;

    @Value("${server.port}")
    private String port;

    @GetMapping("/instance")
    public Map<String, String> instance() {

        return Map.of(
                "instance", instanceId,
                "port", port
        );
    }
}