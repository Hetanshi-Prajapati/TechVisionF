package com.example.signup.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UIRouterController {

    @GetMapping("/my-five-year-plan")
    public String fiveYearPlanPage() {
        return "MyFiveYearPlan";
    }

    @GetMapping("/roadmap")
    public String roadmapSelectionPage() {
        return "Roadmap";
    }

    @GetMapping("/roadmap/details")
    public String roadmapDetailsPage() {
        return "Roadmap";
    }
}
