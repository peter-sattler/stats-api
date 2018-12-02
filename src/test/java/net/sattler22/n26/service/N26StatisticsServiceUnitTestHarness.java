package net.sattler22.n26.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.DelayQueue;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.StopWatch;

import net.sattler22.n26.service.N26StatisticsService.N26StatisticsQueryResult;
import net.sattler22.n26.service.N26StatisticsService.N26StatisticsTransaction;

/**
 * N26 Statistics Service Unit Test Harness
 * 
 * @author Pete Sattler
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class N26StatisticsServiceUnitTestHarness {

    private static final Logger LOGGER = LoggerFactory.getLogger(N26StatisticsServiceUnitTestHarness.class);
    private N26StatisticsService statsService = new N26StatisticsServiceDelayQueueImpl(new DelayQueue<>(), 0);

    @Before
    public void resetStatisticsService() {
        statsService.reset();
    }

    @Test
    public void queryTenTransactionsTestCase() {
        final String[] transactionAmounts = { "1.9", "0", "3", "4", "5", "6", "7", "8", "9", "10" };
        final BigDecimal expectedSum = new BigDecimal("53.9");
        final BigDecimal expectedAverage = new BigDecimal("5.39");
        final BigDecimal expectedMin = BigDecimal.ZERO;
        final BigDecimal expectedMax = BigDecimal.TEN;
        Arrays.stream(transactionAmounts).map(amount -> new N26StatisticsTransaction(new BigDecimal(amount), Duration.ZERO)).forEach(transaction -> statsService.add(transaction));
        final N26StatisticsQueryResult queryResult = statsService.getStatistics();
        checkResult(transactionAmounts.length, expectedSum, expectedAverage, expectedMin, expectedMax, queryResult);
    }

    @Test
    public void queryThousandTransactionsTestCase() {
        final BigDecimal startingAmount = BigDecimal.ONE;
        final BigDecimal incrementAmount = new BigDecimal(".10");     // precision=2, scale=2
        final long limit = 1000;
        final BigDecimal expectedSum = BigDecimal.valueOf(51050L);
        final BigDecimal expectedAverage = BigDecimal.valueOf(51.05);
        final BigDecimal expectedMin = startingAmount.add(incrementAmount);
        final BigDecimal expectedMax = BigDecimal.valueOf(101);
        loadStatistics(startingAmount, incrementAmount, limit);
        final N26StatisticsQueryResult queryResult = statsService.getStatistics();
        checkResult(limit, expectedSum, expectedAverage, expectedMin, expectedMax, queryResult);
    }

    private void loadStatistics(BigDecimal startingAmount, BigDecimal incrementAmount, long limit) {
        LOGGER.info("Loading N26 statistics data using startingAmount [{}], incrementAmount [{}] and limit [{}]", startingAmount, incrementAmount, limit);
        final StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        BigDecimal amount = startingAmount;
        for (int i = 0; i < limit; i++) {
            amount = amount.add(incrementAmount);
            final N26StatisticsTransaction transaction = new N26StatisticsTransaction(amount, Duration.ZERO);
            statsService.add(transaction);
            LOGGER.info("Added #{}: {}", NumberFormat.getInstance().format(i + 1), transaction);
        }
        stopWatch.stop();
        LOGGER.info("N26 data load complete, elapsed time: {}", stopWatch.shortSummary());
    }

    public static void checkResult(long expectedCount, BigDecimal expectedSum, BigDecimal expectedAverage, BigDecimal expectedMin, BigDecimal expectedMax, N26StatisticsQueryResult queryResult) {
        assertEquals(expectedCount, queryResult.getCount());
        assertThat(expectedSum, Matchers.comparesEqualTo(queryResult.getSum()));
        assertThat(expectedAverage, Matchers.comparesEqualTo(queryResult.calculateAverage(2, RoundingMode.HALF_UP)));
        assertThat(expectedMin, Matchers.comparesEqualTo(queryResult.getMin()));
        assertThat(expectedMax, Matchers.comparesEqualTo(queryResult.getMax()));
    }
}
