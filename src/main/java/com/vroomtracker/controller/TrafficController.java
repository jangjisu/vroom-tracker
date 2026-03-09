package com.vroomtracker.controller;

import com.vroomtracker.service.TrafficService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Controller
@RequiredArgsConstructor
public class TrafficController {

    private final TrafficService trafficService;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm:ss");

    @GetMapping("/")
    public String index(Model model) {
        TrafficService.DashboardData data = trafficService.getDashboardData(20);

        model.addAttribute("summary", data.summary());
        model.addAttribute("tollGates", data.ranking());
        model.addAttribute("hourlyPattern", data.hourlyPattern());
        model.addAttribute("currentHour", LocalDateTime.now().getHour());
        model.addAttribute("pageLoadTime", LocalDateTime.now().format(FORMATTER));

        return "index";
    }
}
