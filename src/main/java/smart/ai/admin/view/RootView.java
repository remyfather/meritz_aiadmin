package smart.ai.admin.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;
import smart.ai.admin.util.Utility;

@Controller
public class RootView {
    private static final Logger log = LoggerFactory.getLogger(RootView.class);

    @GetMapping("/")
    public Mono<Rendering> rootView(ServerWebExchange exchange, Model model) {
        Utility.setUserInfo(exchange, model);
        return Mono.just(Rendering.view("home").build());
    }

    @GetMapping("/login")
    public Mono<Rendering> loginView() {
        return Mono.just(Rendering.view("login").build());
    }
}
