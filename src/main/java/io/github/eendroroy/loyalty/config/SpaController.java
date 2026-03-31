package io.github.eendroroy.loyalty.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Forwards all client-side React Router paths to the SPA entry point (index.html).
 * <p>
 * Spring MVC resolves {@code @RestController} handlers before this generic catch-all,
 * so {@code /api/**} and {@code /actuator/**} are never intercepted here.
 * Static asset paths (containing a dot, e.g. {@code /assets/main.abc123.js}) are served
 * directly by Spring Boot's static-resource handler and therefore also bypass this controller.
 * </p>
 */
@Controller
public class SpaController {

    @GetMapping(value = {
            "/",
            "/dashboard",
            "/dashboard/**",
            "/data-sources",
            "/data-sources/**",
            "/archived-sources",
            "/archived-sources/**",
            "/imported-data",
            "/imported-data/**",
            "/monitor",
            "/monitor/**",
            "/rules",
            "/rules/**",
    })
    public String spa() {
        return "forward:/index.html";
    }
}

