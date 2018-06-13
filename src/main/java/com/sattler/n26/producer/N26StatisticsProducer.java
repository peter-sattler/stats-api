package com.sattler.n26.producer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sattler.n26.service.N26StatisticsService;
import com.sattler.n26.service.N26StatisticsService.N26StatisticsTransaction;

/**
 * N26 Statistics Producer
 * 
 * @author Pete Sattler
 * @implSpec This class is immutable and thread-safe
 */
public final class N26StatisticsProducer implements Callable<Integer> {

    private final Logger LOGGER = LoggerFactory.getLogger(N26StatisticsProducer.class);
    private final BigDecimal incrementAmount;
    private final Duration messageDelay;
    private final int outOfOrderThreshold;
    private final Duration outOfOrderMessageDelayAdjustment;
    private final long sleepIntervalSeconds;
    private final N26StatisticsService statsService;
    private final Object lockObject = new Object();

    /**
     * Constructs a new N26 statistics producer (including out of order transactions)
     * 
     * @param incrementAmount The transaction increment amount
     * @param messageDelay The delay amount before which the transaction is available for consumption
     * @param outOfOrderThreshold The threshold for generating an out of order transaction
     * @param outOfOrderMessageDelayAdjustment The out of order message delay duration adjustment
     * @param sleepIntervalSeconds The number of seconds to sleep between each generated transaction
     * @param statsService The N26 statistics service
     */
    public N26StatisticsProducer(BigDecimal incrementAmount, Duration messageDelay, int outOfOrderThreshold, Duration outOfOrderMessageDelayAdjustment, long sleepIntervalSeconds, N26StatisticsService statsService) {
        super();
        this.incrementAmount = incrementAmount;
        this.messageDelay = messageDelay;
        this.outOfOrderThreshold = outOfOrderThreshold;
        this.outOfOrderMessageDelayAdjustment = outOfOrderMessageDelayAdjustment;
        this.sleepIntervalSeconds = sleepIntervalSeconds;
        this.statsService = statsService;
    }

    /**
     * Constructs a new N26 statistics producer (no out of order transactions)
     * 
     * @param incrementAmount The transaction increment amount
     * @param messageDelay The delay amount before which the transaction is available for consumption
     * @param sleepIntervalSeconds The number of seconds to sleep between each generated transaction
     * @param statsService The N26 statistics service
     */
    public N26StatisticsProducer(BigDecimal incrementAmount, Duration messageDelay, long sleepIntervalSeconds, N26StatisticsService statsService) {
        this(incrementAmount, messageDelay, 0, Duration.ZERO, sleepIntervalSeconds, statsService);
    }

    /**
     * Produces and adds a single N26 transaction to the statistics service
     * 
     * @startingAmount The transaction starting amount
     */
    public void produce(BigDecimal startingAmount) {
        produce(0, startingAmount);
    }

    /**
     * Produces and adds a multiple N26 transactions to the statistics service
     * 
     * @return The number of transactions produced
     */
    @Override
    public Integer call() throws Exception {
        int counter = 0;
        BigDecimal amount = BigDecimal.ZERO;
        while (!Thread.currentThread().isInterrupted()) {
            produce(++counter, amount);
            try {
                TimeUnit.SECONDS.sleep(sleepIntervalSeconds);
            } catch (InterruptedException e) {
                // Get out of loop:
                Thread.currentThread().interrupt();
            }
        }
        return new Integer(counter);
    }

    private void produce(int count, BigDecimal startingAmount) {
        final BigDecimal amount;
        final Duration delay;
        final N26StatisticsTransaction transaction;
        synchronized (lockObject) {
            amount = startingAmount.add(incrementAmount);
            if (outOfOrderThreshold > 0 && count % outOfOrderThreshold == 0) {
                delay = messageDelay.minus(outOfOrderMessageDelayAdjustment);
            } else {
                delay = messageDelay;
            }
            transaction = new N26StatisticsTransaction(amount, delay);
            statsService.add(transaction);
        }
        LOGGER.info("Added: [{}]", transaction);
    }

    public BigDecimal getIncrementAmount() {
        return incrementAmount;
    }

    public Duration getMessageDelay() {
        return messageDelay;
    }

    public int getOutOfOrderThreshold() {
        return outOfOrderThreshold;
    }

    public Duration getOutOfOrderMessageDelayAdjustment() {
        return outOfOrderMessageDelayAdjustment;
    }

    public long getSleepIntervalSeconds() {
        return sleepIntervalSeconds;
    }

    /**
     * Started check
     * 
     * @return True if started and producing transactions. Otherwise, returns false.
     */
    public boolean hasStarted() {
        return statsService.hasTransactions();
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
