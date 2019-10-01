package net.sattler22.stats.producer;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sattler22.stats.service.StatisticsService;

/**
 * Statistics Producer Configuration
 *
 * @author Pete Sattler
 */
@Configuration
public class StatisticsProducerConfig {

    @Autowired
    private StatisticsProducerProperties statsProducerProperties;

    @Autowired
    private StatisticsService statsService;

    @Bean(name = { "statisticsProducer" })
    public StatisticsProducer createStatisticsProducer() {
        final BigDecimal incrementAmount = statsProducerProperties.getIncrementAmount();
        final Duration messageDelay = statsProducerProperties.getMessageDelay();
        final int outOfOrderThreshold = statsProducerProperties.getOutOfOrderThreshold();
        final Duration outOfOrderMessageDelayAdjustment = statsProducerProperties.getOutOfOrderMessageDelayAdjustment();
        final long sleepIntervalSeconds = statsProducerProperties.getSleepIntervalSeconds();
        final StatisticsProducer impl = new StatisticsProducer(incrementAmount, messageDelay, outOfOrderThreshold, outOfOrderMessageDelayAdjustment, sleepIntervalSeconds, statsService);
        return impl;
    }
}
