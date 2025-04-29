package net.sattler22.stats.config;

import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Real-Time Statistics Service Configuration
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 * @version May 2025
 */
@Configuration
public class StatisticsServiceConfig {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceConfig.class);

    @Bean
    public StatisticsService statisticsService(StatisticsServiceProperties statsServiceProperties) {
        logger.info("Transaction Expiry Interval: {} seconds", statsServiceProperties.expiryInterval().toSeconds());
        logger.info("Transaction Expiry Clean-up Interval: {} seconds", statsServiceProperties.expiryCleanUpInterval().toSeconds());
        return new StatisticsServiceImpl(statsServiceProperties.expiryInterval());
    }
}
