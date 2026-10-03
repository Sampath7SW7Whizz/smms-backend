package com.smms.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardingController {

    @GetMapping(value = {
            "/login",
            "/register",
            "/forgot-password",
            "/privacy-policy",
            "/terms-of-service",
            "/refund-policy",
            "/cookie-policy",
            "/security-policy",
            "/dashboard",
            "/superadmindashboard",
            "/inventory",
            "/shop",
            "/profile",
            "/admin/login"
    })
    public String forwardToIndex() {
        return "forward:/index.html";
    }
}
