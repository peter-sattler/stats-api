package com.sattler.n26.producer;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * N26 Statistics Producer Properties Configuration
 * 
 * @author Pete Sattler
 */
@Configuration
@EnableConfigurationProperties
public class N26StatisticsProducerPropertiesConfig {

    @Bean(name = { "n26StatisticsProducerProperties" })
    @ConfigurationProperties(prefix = "n26.statistics.producer")
    public N26StatisticsProducerProperties createN26StatisticsProducerProperties() {
        return new N26StatisticsProducerProperties();
    }
}
