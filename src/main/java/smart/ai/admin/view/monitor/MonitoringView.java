package smart.ai.admin.view.monitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import smart.ai.admin.util.Utility;

@Controller
@RequestMapping("/monitor")
public class MonitoringView {

    private static final Logger log = LoggerFactory.getLogger(MonitoringView.class);

    @GetMapping("/daily")
    public Mono<Rendering> dailyView(ServerWebExchange exchange, Model model) {
        Utility.setUserInfo(exchange, model);
        return Mono.just(Rendering.view("monitor/daily").build());
    }

    @GetMapping("/gpu-daily")
    public Mono<Rendering> dailyGpuView(ServerWebExchange exchange, Model model) {
        Utility.setUserInfo(exchange, model);
        return Mono.just(Rendering.view("monitor/gpu-daily").build());
    }

    @GetMapping("/period")
    public Mono<Rendering> periodView(ServerWebExchange exchange, Model model) {
        Utility.setUserInfo(exchange, model);
        return Mono.just(Rendering.view("monitor/period").build());
    }

    @GetMapping("/detail")
    public Mono<Rendering> detailView(ServerWebExchange exchange, Model model) {
        Utility.setUserInfo(exchange, model);
        return Mono.just(Rendering.view("monitor/detail").build());
    }
}
