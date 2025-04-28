// src/main/java/com/bizket/trend/controller/TrendController.java
package com.bizket.trend.controller;

import com.bizket.trend.dto.MonthlyTrend;
import com.bizket.trend.service.TrendService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/trends")
public class TrendController {

    private final TrendService trendService;


    public TrendController(TrendService trendService) {
        this.trendService = trendService;
    }

    @GetMapping("/{kw}/monthly")
    public List<MonthlyTrend> getMonthlyTrend(@PathVariable("kw") String kw) {
        return trendService.fetchMonthlyTrend(kw);
    }
}
