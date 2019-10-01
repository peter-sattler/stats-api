package net.sattler22.stats.service;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

import net.jcip.annotations.Immutable;

/**
 * Statistics Service Delay Queue Implementation
 *
 * @author Pete Sattler
 */
@Immutable
public final class StatisticsServiceDelayQueueImpl implements StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsServiceDelayQueueImpl.class);
    private final DelayQueue<StatisticsTransaction> queue;  //Thread-safe by interface contract
    private final long maximumDelaySeconds;

    /**
     * Constructs a new statistics service delay queue implementation
     *
     * @param queue The input queue
     * @param maximumDelaySeconds The maximum allowed delay (in seconds)
     */
    public StatisticsServiceDelayQueueImpl(DelayQueue<StatisticsTransaction> queue, long maximumDelaySeconds) {
        super();
        this.queue = queue;
        this.maximumDelaySeconds = maximumDelaySeconds;
    }

    @Override
    public void add(StatisticsTransaction transaction) throws IllegalArgumentException {
        if (transaction.getDelay(SECONDS) > maximumDelaySeconds) {
            throw new IllegalArgumentException(String.format("Delay exceeds [%s] seconds", maximumDelaySeconds));
        }
        queue.offer(transaction);
    }

    @Override
    public boolean hasTransactions() {
        return queue.peek() != null;
    }

    @Override
    public StatisticsQueryResult getStatistics() {
        //Drain to adds to the list's tail so its OVERALL complexity will be equivalent to O(1).
        //NOTE: This is an amortized constant-time operation because it must resize itself as needed.
        //TODO: An exception could be thrown half-way through draining!!!
        final List<StatisticsTransaction> snapshot = new ArrayList<>();
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int nbrTransactions = queue.drainTo(snapshot);
        stopWatch.stop();
        LOGGER.info("DRAIN-TO elapsed time: {}", stopWatch.shortSummary());
        if (nbrTransactions == 0) {
            return new StatisticsQueryResult(ZERO, nbrTransactions, ZERO, ZERO);
        }
        final StatisticsQueryResult arrayListGetResult = getMetricsViaArrayListGet(snapshot, nbrTransactions);
        LOGGER.info("Query result via ArrayList.get(): [{}]", arrayListGetResult);
        return arrayListGetResult;
    }

    /**
     * Get metrics using {@code ArrayList.get()} since its an O(1) operation
     */
    private StatisticsQueryResult getMetricsViaArrayListGet(List<StatisticsTransaction> snapshot, int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        BigDecimal sum = ZERO;
        BigDecimal min = snapshot.get(0).getAmount();
        BigDecimal max = ZERO;
        for (int i = 0; i < count; i++) {
            final StatisticsTransaction transaction = snapshot.get(i);
            final BigDecimal amount = transaction.getAmount();
            sum = sum.add(amount);
            if (amount.compareTo(min) < 0) {
                min = amount;
            }
            if (amount.compareTo(max) > 0) {
                max = amount;
            }
        }
        stopWatch.stop();
        LOGGER.info("ArrayList.get() elapsed time: {}", stopWatch.shortSummary());
        return new StatisticsQueryResult(sum, count, min, max);
    }

    @Override
    public void reset() {
        queue.clear();
    }

    @Override
    public String toString() {
        return String.format("%s [queue=%s, maximumDelaySeconds=%s]",
                              getClass().getSimpleName(), queue, maximumDelaySeconds);
    }
}
