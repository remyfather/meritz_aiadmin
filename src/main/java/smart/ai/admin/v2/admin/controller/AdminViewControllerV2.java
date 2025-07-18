package smart.ai.admin.v2.auth.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminViewControllerV2 {

    @GetMapping("/admin/dashboard")
    public String adminDashboard() {
        return "admin/dashboard";
    }

    @GetMapping("/admin")
    public String adminRedirect() {
        return "redirect:/admin/dashboard";
    }
} 