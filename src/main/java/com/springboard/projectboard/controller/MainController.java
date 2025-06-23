package com.springboard.projectboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

    @GetMapping("/")
    public String root() {
        return "forward:/articles";
    }

    @GetMapping("/health")
    @ResponseBody
    public String healthCheck() {
        return "Success Health Check";
    }
}
