package net.sattler22.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * Statistics Application Entry Point
 *
 * @author Pete Sattler
 */
@SpringBootApplication
@ComponentScan({ "net.sattler22.stats" })
public class StatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsApplication.class, args);
    }
}
