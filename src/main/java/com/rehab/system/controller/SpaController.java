package com.rehab.system.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaController {

    @GetMapping({
            "/", "/login", "/dashboard", "/patients", "/doctors", "/beds",
            "/assessment-items", "/treatment-items", "/assessment-tasks",
            "/assessment-records", "/treatment-records", "/patient-trend",
            "/users", "/audit-logs", "/patient-home"
    })
    public String index() {
        return "forward:/index.html";
    }
}
