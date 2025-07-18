package smart.ai.admin.util;

import org.springframework.ui.Model;
import org.springframework.web.server.ServerWebExchange;

public class Utility {
    public static void setUserInfo(ServerWebExchange exchange, Model model) {
        model.addAttribute("userRole", exchange.getAttributes().get("userRole"));
        model.addAttribute("userName", exchange.getAttributes().get("userName"));
    }
}
