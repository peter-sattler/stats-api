package net.sattler22.stats.producer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Statistics Producer Properties Configuration
 *
 * @author Pete Sattler
 */
@Configuration
@EnableConfigurationProperties
public class StatisticsProducerPropertiesConfig {

    @Bean(name = { "statisticsProducerProperties" })
    @ConfigurationProperties(prefix = "stats.api.producer")
    public StatisticsProducerProperties createStatisticsProducerProperties() {
        return new StatisticsProducerProperties();
    }
}
