package net.sattler22.stats.service;

import jakarta.validation.constraints.NotNull;
import net.jcip.annotations.Immutable;
import net.sattler22.stats.exception.ExpirationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;

/**
 * Real-Time Statistics Service Implementation
 *
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 * @version May 2025
 */
@Immutable
public final class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private static final String TRANSACTION_EXPIRED_ERROR_MESSAGE_TEMPLATE = "%s has expired";
    private final long expiryIntervalSecs;
    private final List<StatisticsTransaction> transactions = Collections.synchronizedList(new LinkedList<>());

    /**
     * Constructs a new statistics service
     *
     * @param expiryInterval The real-time transaction expiration interval
     */
    public StatisticsServiceImpl(Duration expiryInterval) {
        this.expiryIntervalSecs = expiryInterval.toSeconds();
    }

    @Override
    public void add(@NotNull StatisticsTransaction transaction) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            if (transaction.isExpired(expiryIntervalSecs))
                throw new ExpirationException(String.format(TRANSACTION_EXPIRED_ERROR_MESSAGE_TEMPLATE, transaction));
            transactions.addFirst(transaction);  //Add to head
            stopWatch.stop();
            logger.info("Added {}, elapsed time: {} ns", transaction, stopWatch.getTotalTimeNanos());
        }
        finally {
            if (stopWatch.isRunning())
                stopWatch.stop();
        }
    }

    @Override
    public boolean hasTransactions() {
        for (final StatisticsService.StatisticsTransaction transaction : transactions)
            if (!transaction.isExpired(expiryIntervalSecs))
                return true;
        return false;
    }

    @Override
    public StatisticsQueryResult collect(int calcScale, RoundingMode calcRoundingMode) {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            //Grab an array snapshot that can be accessed in constant time:
            final StatisticsService.StatisticsTransaction[] snapshot = transactions.toArray(new StatisticsTransaction[0]);
            BigDecimal sum = ZERO;
            BigDecimal max = ZERO;
            BigDecimal min = null;
            long count = 0L;
            for (final StatisticsService.StatisticsTransaction transaction : snapshot)
                if (!transaction.isExpired(expiryIntervalSecs)) {
                    sum = sum.add(transaction.amount());
                    if (transaction.amount().compareTo(max) > 0)
                        max = transaction.amount();
                    if (min == null || transaction.amount().compareTo(min) < 0)
                        min = transaction.amount();
                    count++;
                }
            BigDecimal average = ZERO;
            if (count > 0)
                average = sum.divide(BigDecimal.valueOf(count), calcScale, calcRoundingMode);
            if (min == null)
                min = ZERO;
            final StatisticsQueryResult queryResult = new StatisticsQueryResult(sum, average, max, min, count);
            stopWatch.stop();
            logger.info("{} using rounding mode [{}], elapsed time: {} ns", queryResult, calcRoundingMode, stopWatch.getTotalTimeNanos());
            return queryResult;
        }
        finally {
            if (stopWatch.isRunning())
                stopWatch.stop();
        }
    }

    @Override
    @Scheduled(fixedDelayString = "${stats-api.service.expiry-clean-up-interval}", timeUnit = TimeUnit.SECONDS)
    public void removeIfExpired() {
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            final int count = transactions.size();
            if (transactions.removeIf(x -> x.isExpired(expiryIntervalSecs))) {
                stopWatch.stop();
                logger.info("Removed [{}] expired transaction{}, elapsed time: {} ns",
                        count - transactions.size(), count - transactions.size() == 1 ? "" : "s", stopWatch.getTotalTimeNanos());
            }
        }
        finally {
            if (stopWatch.isRunning())
                stopWatch.stop();
        }
    }

    @Override
    public String toString() {
        return String.format("%s [transactions=%s]", getClass().getSimpleName(), transactions);
    }
}
