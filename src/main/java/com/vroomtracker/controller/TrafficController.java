package com.vroomtracker.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TrafficController {

    private final String naverMapsNcpKeyId;

    public TrafficController(@Value("${naver.maps.ncp-key-id:}") String naverMapsNcpKeyId) {
        this.naverMapsNcpKeyId = naverMapsNcpKeyId;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("naverMapsNcpKeyId", naverMapsNcpKeyId);
        return "index";
    }
}
