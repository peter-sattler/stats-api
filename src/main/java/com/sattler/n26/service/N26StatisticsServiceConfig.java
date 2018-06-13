package com.sattler.n26.service;

import java.util.concurrent.DelayQueue;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * N26 Statistics Service Configuration
 * 
 * @author Pete Sattler
 */
@Configuration
public class N26StatisticsServiceConfig {

    @Bean(name = { "n26StatisticsService" })
    public N26StatisticsService createN26StatisticsService() {
        final N26StatisticsServiceDelayQueueImpl impl = new N26StatisticsServiceDelayQueueImpl(new DelayQueue<>(), 60);
        return impl;
    }
}
