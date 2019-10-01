package net.sattler22.stats.producer;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.jcip.annotations.Immutable;
import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;

/**
 * Statistics Producer
 *
 * @author Pete Sattler
 */
@Immutable
public final class StatisticsProducer implements Callable<Integer> {

    private final Logger LOGGER = LoggerFactory.getLogger(StatisticsProducer.class);
    private final BigDecimal incrementAmount;
    private final Duration messageDelay;
    private final int outOfOrderThreshold;
    private final Duration outOfOrderMessageDelayAdjustment;
    private final long sleepIntervalSeconds;
    private final StatisticsService statsService;
    private final Object lockObject = new Object();

    /**
     * Constructs a new statistics producer (including out of order transactions)
     *
     * @param incrementAmount The transaction increment amount
     * @param messageDelay The delay amount before which the transaction is available for consumption
     * @param outOfOrderThreshold The threshold for generating an out of order transaction
     * @param outOfOrderMessageDelayAdjustment The out of order message delay duration adjustment
     * @param sleepIntervalSeconds The number of seconds to sleep between each generated transaction
     * @param statsService The statistics service
     */
    public StatisticsProducer(BigDecimal incrementAmount, Duration messageDelay, int outOfOrderThreshold, Duration outOfOrderMessageDelayAdjustment, long sleepIntervalSeconds, StatisticsService statsService) {
        super();
        this.incrementAmount = incrementAmount;
        this.messageDelay = messageDelay;
        this.outOfOrderThreshold = outOfOrderThreshold;
        this.outOfOrderMessageDelayAdjustment = outOfOrderMessageDelayAdjustment;
        this.sleepIntervalSeconds = sleepIntervalSeconds;
        this.statsService = statsService;
    }

    /**
     * Constructs a new statistics producer (no out of order transactions)
     *
     * @param incrementAmount The transaction increment amount
     * @param messageDelay The delay amount before which the transaction is available for consumption
     * @param sleepIntervalSeconds The number of seconds to sleep between each generated transaction
     * @param statsService The statistics service
     */
    public StatisticsProducer(BigDecimal incrementAmount, Duration messageDelay, long sleepIntervalSeconds, StatisticsService statsService) {
        this(incrementAmount, messageDelay, 0, Duration.ZERO, sleepIntervalSeconds, statsService);
    }

    /**
     * Produces and adds a single transaction to the statistics service
     *
     * @startingAmount The transaction starting amount
     */
    public void produce(BigDecimal startingAmount) {
        produce(0, startingAmount);
    }

    /**
     * Produces and adds a multiple transactions to the statistics service
     *
     * @return The number of transactions produced
     */
    @Override
    public Integer call() throws Exception {
        int counter = 0;
        BigDecimal amount = ZERO;
        while (!Thread.currentThread().isInterrupted()) {
            produce(++counter, amount);
            try {
                SECONDS.sleep(sleepIntervalSeconds);
            } catch (InterruptedException e) {
                //Get out of loop:
                Thread.currentThread().interrupt();
            }
        }
        return new Integer(counter);
    }

    private void produce(int count, BigDecimal startingAmount) {
        final BigDecimal amount;
        final Duration delay;
        final StatisticsTransaction transaction;
        synchronized (lockObject) {
            amount = startingAmount.add(incrementAmount);
            if (outOfOrderThreshold > 0 && count % outOfOrderThreshold == 0) {
                delay = messageDelay.minus(outOfOrderMessageDelayAdjustment);
            } else {
                delay = messageDelay;
            }
            transaction = new StatisticsTransaction(amount, delay);
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
        return String.format("%s [incrementAmount=%s, messageDelay=%s, outOfOrderThreshold=%s, outOfOrderMessageDelayAdjustment=%s, sleepIntervalSeconds=%s, statsService=%s]",
                             getClass().getSimpleName(), incrementAmount, messageDelay, outOfOrderThreshold, outOfOrderMessageDelayAdjustment, sleepIntervalSeconds, statsService);
    }
}
