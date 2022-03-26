package net.sattler22.stats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Real-Time Statistics Application
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 */
@SpringBootApplication
@EnableScheduling
public class StatisticsApplication {

    public static void main(String[] args) {
        SpringApplication.run(StatisticsApplication.class, args);
    }
}
