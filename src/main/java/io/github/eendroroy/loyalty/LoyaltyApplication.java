package io.github.eendroroy.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LoyaltyApplication {
    static void main(String[] args) {
        SpringApplication.run(LoyaltyApplication.class, args);
    }
}
