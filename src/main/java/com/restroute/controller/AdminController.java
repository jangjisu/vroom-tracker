package com.restroute.controller;

import com.restroute.common.ApiResponse;
import com.restroute.service.RestStopServiceAreaCodeBackfillService;
import com.restroute.service.admindashboard.AdminDashboardService;
import com.restroute.service.admindashboard.AdminDashboardSummary;
import com.restroute.service.salesranking.SalesRankingUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class AdminController {

    private final SalesRankingUploadService salesRankingUploadService;
    private final RestStopServiceAreaCodeBackfillService backfillService;
    private final AdminDashboardService dashboardService;

    @GetMapping("/admin")
    public String admin() {
        return "admin";
    }

    @GetMapping("/api/admin/dashboard")
    @ResponseBody
    public ApiResponse<AdminDashboardSummary> dashboard() {
        return ApiResponse.success(dashboardService.getSummary());
    }

    @PostMapping("/admin/sales-rankings/products")
    public String uploadProductSalesRankings(@RequestParam("productFile") MultipartFile productFile) {
        salesRankingUploadService.uploadProducts(productFile);
        return "redirect:/admin?upload=success";
    }

    @PostMapping("/admin/sales-rankings/stores")
    public String uploadStoreSalesRankings(@RequestParam("storeFile") MultipartFile storeFile) {
        salesRankingUploadService.uploadStores(storeFile);
        return "redirect:/admin?upload=success";
    }

    @PostMapping("/admin/sales-rankings/backfill")
    public String backfillSalesRankings() {
        backfillService.backfill();
        return "redirect:/admin?backfill=success";
    }
}
