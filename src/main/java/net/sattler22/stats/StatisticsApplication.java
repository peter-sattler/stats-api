package net.sattler22.stats;

import net.sattler22.stats.config.StatisticsServiceProperties;
import net.sattler22.stats.config.SwaggerProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.text.NumberFormat;

/**
 * Real-Time Statistics Application
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 * @version May 2025
 */
@SpringBootApplication
@EnableConfigurationProperties(value = {
        StatisticsServiceProperties.class,
        SwaggerProperties.class
})
@EnableScheduling
public class StatisticsApplication {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StatisticsApplication.class, args);
        logMemory();
    }

    private static void logMemory() {
        if (logger.isInfoEnabled()) {
            final Runtime runtime = Runtime.getRuntime();
            final NumberFormat numberFormat = NumberFormat.getInstance();
            final long maxMemory = runtime.maxMemory();
            final long allocatedMemory = runtime.totalMemory();
            final long freeMemory = runtime.freeMemory();
            final int mb = 1024 * 1024;
            logger.info("-------- MEMORY INFO --------");
            logger.info("Free memory: {} MB", numberFormat.format(freeMemory / mb));
            logger.info("Allocated memory: {} MB", numberFormat.format(allocatedMemory / mb));
            logger.info("Max memory: {} MB", numberFormat.format(maxMemory / mb));
            logger.info("Total free memory: {} MB", numberFormat.format((freeMemory + (maxMemory - allocatedMemory)) / mb));
            logger.info("-----------------------------");
        }
    }
}
