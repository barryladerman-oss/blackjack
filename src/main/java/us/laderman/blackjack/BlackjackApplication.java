package us.laderman.blackjack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main entry point for the Blackjack Spring Boot application.
 * Enables scheduling for periodic tasks.
 */
@SpringBootApplication
@EnableScheduling
public class BlackjackApplication {
    public static void main(String[] args) {
        SpringApplication.run(BlackjackApplication.class, args);
    }
}
