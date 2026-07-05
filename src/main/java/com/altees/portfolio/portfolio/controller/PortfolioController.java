package com.altees.portfolio.portfolio.controller;

import com.altees.portfolio.common.response.ApiResponse;
import com.altees.portfolio.portfolio.dto.PortfolioResponse;
import com.altees.portfolio.portfolio.service.PortfolioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PortfolioResponse>> getPortfolio() {
        return ResponseEntity.ok(ApiResponse.ok(portfolioService.getPortfolio()));
    }
}
