package net.sattler22.stats.service;

import static java.math.BigDecimal.ZERO;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StopWatch;

import net.jcip.annotations.Immutable;

/**
 * Real-Time Statistics Service Implementation
 *
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 */
@Immutable
public final class StatisticsServiceImpl implements StatisticsService {

    private static final Logger logger = LoggerFactory.getLogger(StatisticsServiceImpl.class);
    private static final String TRANSACTION_EXPIRED_ERROR_MESSAGE_TEMPLATE = "%s has expired";
    private final int expiryIntervalMillis;
    private final List<StatisticsTransaction> transactions = Collections.synchronizedList(new LinkedList<>());

    /**
     * Constructs a new statistics service
     *
     * @param expiryIntervalSecs The real-time transaction expiration interval (in seconds)
     */
    StatisticsServiceImpl(int expiryIntervalSecs) {
        this.expiryIntervalMillis = expiryIntervalSecs * 1_000;
    }

    @Override
    public void add(@NotNull StatisticsTransaction transaction) {
        final var stopWatch = new StopWatch();
        stopWatch.start();
        try {
            if (transaction.isExpired(expiryIntervalMillis)) {
                final var errorMessage = String.format(TRANSACTION_EXPIRED_ERROR_MESSAGE_TEMPLATE, transaction);
                logger.warn(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            transactions.add(0, transaction);  //Add to head
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
        for (final var transaction : transactions)
            if (!transaction.isExpired(expiryIntervalMillis))
                return true;
        return false;
    }

    @Override
    public StatisticsQueryResult collect(int calcScale, RoundingMode calcRoundingMode) {
        final var stopWatch = new StopWatch();
        stopWatch.start();
        try {
            //Grab an array snapshot that can be accessed in constant time:
            final var snapshot = transactions.toArray(new StatisticsTransaction[transactions.size()]);
            var sum = ZERO;
            var max = ZERO;
            BigDecimal min = null;
            var count = 0L;
            for (final var transaction : snapshot)
                if (!transaction.isExpired(expiryIntervalMillis)) {
                    sum = sum.add(transaction.amount());
                    if (transaction.amount().compareTo(max) > 0)
                        max = transaction.amount();
                    if (min == null || transaction.amount().compareTo(min) < 0)
                        min = transaction.amount();
                    count++;
                }
            var average = ZERO;
            if (count > 0)
                average = sum.divide(BigDecimal.valueOf(count), calcScale, calcRoundingMode);
            if (min == null)
                min = ZERO;
            final var queryResult = new StatisticsQueryResult(sum, average, max, min, count);
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
    @Scheduled(fixedDelayString = "${stats-api.service.expiry-clean-up-interval-secs}", timeUnit = TimeUnit.SECONDS)
    public void removeExpired() {
        final var stopWatch = new StopWatch();
        stopWatch.start();
        try {
            final var count = transactions.size();
            if (transactions.removeIf(x -> x.isExpired(expiryIntervalMillis))) {
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
