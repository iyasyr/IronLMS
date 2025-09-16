package com.ironhack.lms.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class WhoAmIController {
    @GetMapping("/api/me")
    public Map<String,Object> me(Authentication auth) {
        return Map.of("username", auth.getName(), "authorities", auth.getAuthorities());
    }
}
