package net.sattler22.stats.service;

import net.sattler22.stats.exception.ExpirationException;
import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;
import net.sattler22.stats.util.TestUtils;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.Arrays;

import static java.math.BigDecimal.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Real-Time Statistics Service Unit Test Harness
 *
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 * @version May 2025
 */
final class StatisticsServiceUnitTest {

    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final int CALC_SCALE = 9;
    private static final RoundingMode CALC_ROUNDING_MODE= RoundingMode.HALF_UP;
    private static final Duration EXPIRY_INTERVAL = Duration.ofSeconds(5);
    private StatisticsService statsService;

    @BeforeEach
    void init() {
        statsService = new StatisticsServiceImpl(EXPIRY_INTERVAL);
    }

    @Test
    void testAddTransactionSuccess() {
        addTransactionImpl(AMOUNT, 1);
        assertEquals(1L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testAddTransactionFailsWhenTransactionIsNull() {
        assertThrows(NullPointerException.class, () -> statsService.add(null));
    }

    @Test
    void testAddTransactionFailsWhenTransactionIsExpired() {
        final long expiredTimeStamp = TestUtils.epoch() - EXPIRY_INTERVAL.toSeconds() - 1L;
        final StatisticsTransaction expiredTransaction = new StatisticsTransaction(AMOUNT, expiredTimeStamp);
        assertThrows(ExpirationException.class, () -> statsService.add(expiredTransaction));
    }

    @Test
    void testHasTransactionsReturnsFalseWithNoTransactions() {
        assertFalse(statsService.hasTransactions());
    }

    @Test
    void testHasTransactionsReturnsTrueWithSingleTransaction() {
        addTransactionImpl(AMOUNT, 1);
        assertTrue(statsService.hasTransactions());
    }

    @Test
    void testHasTransactionsReturnsTrueWithMultipleTransactions() {
        addTransactionImpl(AMOUNT, 3);
        assertTrue(statsService.hasTransactions());
    }

    @Test
    void testCollectSuccessWithNoTransactions() {
        final StatisticsService.StatisticsQueryResult queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(ZERO, ZERO, ZERO, ZERO, 0L, queryResult);
    }

    @Test
    void testCollectSuccessWithSingleTransaction() {
        addTransactionImpl(AMOUNT, 1);
        final StatisticsService.StatisticsQueryResult queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(TEN, TEN, TEN, TEN, 1L, queryResult);
    }

    @Test
    void testCollectSuccessWithTenTransactions() {
        final String[] transactionAmounts = { "1.9", "0", "3", "4", "5", "6", "7", "8", "9", "10" };
        final BigDecimal expectedSum = new BigDecimal("53.90");
        final BigDecimal expectedAverage = new BigDecimal("5.39");
        Arrays.stream(transactionAmounts)
              .map(amount -> new StatisticsTransaction(new BigDecimal(amount), TestUtils.epoch()))
              .forEach(transaction -> statsService.add(transaction));
        final StatisticsService.StatisticsQueryResult queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(expectedSum, expectedAverage, TEN, ZERO, transactionAmounts.length, queryResult);
    }

    @Test
    void testCollectSuccessWithOneThousandTransactions() {
        final BigDecimal startingAmount = ONE;
        final BigDecimal incrementAmount = new BigDecimal(".10");
        final BigDecimal expectedSum = new BigDecimal(51050);
        final BigDecimal expectedAverage = new BigDecimal("51.05");
        final BigDecimal expectedMax = new BigDecimal(101);
        final BigDecimal expectedMin = startingAmount.add(incrementAmount);
        final long expectedCount = 1_000L;
        BigDecimal amount = startingAmount;
        for (int i = 0; i < expectedCount; i++) {
            amount = amount.add(incrementAmount);
            statsService.add(new StatisticsTransaction(amount, TestUtils.epoch()));
        }
        final StatisticsService.StatisticsQueryResult queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(expectedSum, expectedAverage, expectedMax, expectedMin, expectedCount, queryResult);
    }

    @Test
    void testCollectFailsWhenAverageIsNonTerminatingAndRoundingModeIsUnnecessary() {
        addTransactionImpl(new BigDecimal(".25"), 2);  //NOTE: precision=2, scale=2
        addTransactionImpl(new BigDecimal(".50"), 1);
        assertThrows(ArithmeticException.class, () -> statsService.collect(CALC_SCALE, RoundingMode.UNNECESSARY));
    }

    private static void assertSuccessQueryResults(final BigDecimal sum, final BigDecimal average, final BigDecimal max,
                                                  final BigDecimal min, final long count, final StatisticsQueryResult queryResult) {
        assertEquals(0, sum.compareTo(queryResult.sum()));  //IMPORTANT: Recommended way to compare two BigDecimals!!!
        assertEquals(0, average.compareTo(queryResult.avg()));
        assertEquals(0, max.compareTo(queryResult.max()));
        assertEquals(0, min.compareTo(queryResult.min()));
        assertEquals(count, queryResult.count());
    }

    @Test
    void testRemoveIfExpiredSuccessWithNoTransactions() {
        statsService.removeIfExpired();
        assertEquals(0L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testRemoveIfExpiredSuccessWithSingleUnexpiredTransaction() {
        addTransactionImpl(AMOUNT, 1);
        statsService.removeIfExpired();
        assertEquals(1L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testRemoveIfExpiredSuccessWhenSingleTransactionExpires() {
        statsService.add(new StatisticsTransaction(AMOUNT, TestUtils.epoch()));
        Awaitility.await().until(() -> !statsService.hasTransactions());
        statsService.removeIfExpired();
        assertEquals(0L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    /**
     * Add one or more transactions
     *
     * @param count The number of transactions to add to the service
     */
    private void addTransactionImpl(final BigDecimal amount, final int count) {
        assert count > 0;
        for (int i = 0; i < count; i++)
            statsService.add(new StatisticsTransaction(amount, TestUtils.epoch() + EXPIRY_INTERVAL.toSeconds()));
    }
}
