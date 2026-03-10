package com.vroomtracker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrafficController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
