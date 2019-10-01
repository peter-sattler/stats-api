package net.sattler22.stats.service;

import java.util.concurrent.DelayQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Statistics Service Configuration
 *
 * @author Pete Sattler
 */
@Configuration
public class StatisticsServiceConfig {

    @Bean(name = { "statisticsService" })
    public StatisticsService createStatisticsService() {
        final StatisticsServiceDelayQueueImpl impl = new StatisticsServiceDelayQueueImpl(new DelayQueue<>(), 60);
        return impl;
    }
}
