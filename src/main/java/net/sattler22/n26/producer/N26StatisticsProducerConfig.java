package net.sattler22.n26.producer;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import net.sattler22.n26.service.N26StatisticsService;

/**
 * N26 Statistics Producer Configuration
 * 
 * @author Pete Sattler
 */
@Configuration
public class N26StatisticsProducerConfig {

    @Autowired
    private N26StatisticsProducerProperties statsProducerProps;

    @Autowired
    private N26StatisticsService statsService;

    @Bean(name = { "n26StatisticsProducer" })
    public N26StatisticsProducer createN26StatisticsProducer() {
        final BigDecimal incrementAmount = statsProducerProps.getIncrementAmount();
        final Duration messageDelay = statsProducerProps.getMessageDelay();
        final int outOfOrderThreshold = statsProducerProps.getOutOfOrderThreshold();
        final Duration outOfOrderMessageDelayAdjustment = statsProducerProps.getOutOfOrderMessageDelayAdjustment();
        final long sleepIntervalSeconds = statsProducerProps.getSleepIntervalSeconds();
        final N26StatisticsProducer impl = new N26StatisticsProducer(incrementAmount, messageDelay, outOfOrderThreshold, outOfOrderMessageDelayAdjustment, sleepIntervalSeconds, statsService);
        return impl;
    }
}
