package net.sattler22.stats.service;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

/**
 * Real-Time Statistics Service Properties
 *
 * @author Pete Sattler
 * @version March 2022
 */
@ConfigurationProperties(prefix = "stats-api.service")
@ConstructorBinding
record StatisticsServiceProperties(int expiryIntervalSecs, int expiryCleanUpIntervalSecs) {
}
