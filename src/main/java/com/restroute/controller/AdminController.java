package com.restroute.controller;

import com.restroute.service.salesranking.SalesRankingUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SalesRankingUploadService salesRankingUploadService;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @PostMapping("/admin/sales-rankings")
    public String uploadSalesRankings(
            @RequestParam("productFile") MultipartFile productFile,
            @RequestParam("storeFile") MultipartFile storeFile) {
        salesRankingUploadService.upload(productFile, storeFile);
        return "redirect:/admin?upload=success";
    }
}
