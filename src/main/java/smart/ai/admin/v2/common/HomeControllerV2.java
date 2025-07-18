package smart.ai.admin.v2.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Slf4j
@Controller
@RequestMapping("/")
public class HomeControllerV2 {
    
    @GetMapping
    public Mono<String> home() {
        log.info("Home page requested");
        return Mono.just("home");
    }
    
    @GetMapping("/login")
    public Mono<String> login() {
        log.info("Login page requested");
        return Mono.just("login");
    }
    

    
    @GetMapping("/monitor")
    public Mono<String> monitor() {
        log.info("Monitor page requested");
        return Mono.just("monitor/daily");
    }
} 