package net.sattler22.n26.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.BinaryOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StopWatch;

/**
 * N26 Statistics Service Delay Queue Implementation
 * 
 * @author Pete Sattler
 * @implSpec This class is immutable and thread-safe
 */
public final class N26StatisticsServiceDelayQueueImpl implements N26StatisticsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(N26StatisticsServiceDelayQueueImpl.class);
    private static final BinaryOperator<BigDecimal> SUMMATION_ACCUMULATOR = (amountX, amountY) -> amountX.add(amountY);
    private final DelayQueue<N26StatisticsTransaction> queue;  // Thread-safe by interface contract
    private final long maximumDelaySeconds;

    /**
     * Constructs a new N26 statistics service delay queue implementation
     * 
     * @param queue The input queue
     * @param maximumDelaySeconds The maximum allowed delay (in seconds)
     */
    public N26StatisticsServiceDelayQueueImpl(DelayQueue<N26StatisticsTransaction> queue, long maximumDelaySeconds) {
        super();
        this.queue = queue;
        this.maximumDelaySeconds = maximumDelaySeconds;
    }

    @Override
    public void add(N26StatisticsTransaction transaction) throws IllegalArgumentException {
        if (transaction.getDelay(TimeUnit.SECONDS) > maximumDelaySeconds) {
            throw new IllegalArgumentException(String.format("Delay exceeds [%s] seconds", maximumDelaySeconds));
        }
        queue.offer(transaction);
    }

    @Override
    public boolean hasTransactions() {
        return queue.peek() != null;
    }

    @Override
    public N26StatisticsQueryResult getStatistics() {
        // Drain to adds to the list's tail so its OVERALL complexity will be equivalent to O(1).
        // NOTE: This is an amortized constant-time operation because it must resize itself as needed.
        // TODO: An exception could be thrown half-way through draining!!!
        final List<N26StatisticsTransaction> snapshot = new ArrayList<>();
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final int nbrTransactions = queue.drainTo(snapshot);
        stopWatch.stop();
        LOGGER.info("DRAIN-TO elapsed time: {}", stopWatch.shortSummary());

        if (nbrTransactions == 0) {
            return new N26StatisticsQueryResult(BigDecimal.ZERO, nbrTransactions, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        // Try two different approaches and see which one is faster:
        final N26StatisticsQueryResult streamsResult = getMetricsViaStreams(snapshot, nbrTransactions);
        LOGGER.info("Query result via streams: [{}]", streamsResult);

        final N26StatisticsQueryResult arrayListGetResult = getMetricsViaArrayListGet(snapshot, nbrTransactions);
        LOGGER.info("Query result via ArrayList.get(): [{}]", arrayListGetResult);

        // GASP: Despite all the hipe, it seems streams lose !!!
        return arrayListGetResult;
    }

    /**
     * Get metrics using parallel streams
     */
    private N26StatisticsQueryResult getMetricsViaStreams(List<N26StatisticsTransaction> snapshot, int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        final BigDecimal sum = snapshot.parallelStream().map(entry -> entry.getAmount()).reduce(BigDecimal.ZERO, SUMMATION_ACCUMULATOR);
        final BigDecimal min = snapshot.parallelStream().map(entry -> entry.getAmount()).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        final BigDecimal max = snapshot.parallelStream().map(entry -> entry.getAmount()).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
        stopWatch.stop();
        LOGGER.info("Streams elapsed time: {}", stopWatch.shortSummary());
        return new N26StatisticsQueryResult(sum, count, min, max);
    }

    /**
     * Get metrics using {@code ArrayList.get()} since its an O(1) operation
     */
    private N26StatisticsQueryResult getMetricsViaArrayListGet(List<N26StatisticsTransaction> snapshot, int count) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal min = snapshot.get(0).getAmount();
        BigDecimal max = BigDecimal.ZERO;
        for (int i = 0; i < count; i++) {
            final N26StatisticsTransaction transaction = snapshot.get(i);
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
        return new N26StatisticsQueryResult(sum, count, min, max);
    }

    @Override
    public void reset() {
        queue.clear();
    }

    @Override
    public String toString() {
        return String.format("%s [queue=%s, maximumDelaySeconds=%s]", getClass().getSimpleName(), queue, maximumDelaySeconds);
    }
}
