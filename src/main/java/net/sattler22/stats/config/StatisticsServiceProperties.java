package net.sattler22.stats.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Real-Time Statistics Service Properties
 *
 * @author Pete Sattler
 * @since March 2022
 * @version May 2025
 */
@ConfigurationProperties(prefix = "stats-api.service")
public record StatisticsServiceProperties(Duration expiryInterval, Duration expiryCleanUpInterval) {
}
