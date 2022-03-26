package net.sattler22.stats.service;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.math.BigDecimal.ZERO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;

/**
 * Real-Time Statistics Service Unit Test Harness
 *
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 */
final class StatisticsServiceUnitTest {

    private static final BigDecimal AMOUNT = BigDecimal.TEN;
    private static final int CALC_SCALE = 9;
    private static final RoundingMode CALC_ROUNDING_MODE= RoundingMode.HALF_UP;
    private static final int EXPIRY_INTERVAL_SECS = 7;
    private StatisticsService statsService;

    @BeforeEach
    void init() {
        statsService = new StatisticsServiceImpl(EXPIRY_INTERVAL_SECS);
    }

    @Test
    void testAddTransactionSuccess() {
        addTransactionImpl(AMOUNT, 1);
        assertEquals(1L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testAddTransactionFailsWhenTransactionIsNull() {
        assertThrows(NullPointerException.class, () -> {
            statsService.add(null);
        });
    }

    @Test
    void testAddTransactionFailsWhenTransactionIsExpired() {
        final var expiredTimeStamp = System.currentTimeMillis() - (EXPIRY_INTERVAL_SECS * 1_000L) - 1L;
        final var expiredTransaction = new StatisticsTransaction(AMOUNT, expiredTimeStamp);
        assertThrows(IllegalArgumentException.class, () -> {
            statsService.add(expiredTransaction);
        });
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
        final var queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(ZERO, ZERO, ZERO, ZERO, 0L, queryResult);
    }

    @Test
    void testCollectSuccessWithSingleTransaction() {
        addTransactionImpl(AMOUNT, 1);
        final var queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(TEN, TEN, TEN, TEN, 1L, queryResult);
    }

    @Test
    void testCollectSuccessWithTenTransactions() {
        final String[] transactionAmounts = { "1.9", "0", "3", "4", "5", "6", "7", "8", "9", "10" };
        final var expectedSum = new BigDecimal("53.90");
        final var expectedAverage = new BigDecimal("5.39");
        final var expectedMax = TEN;
        final var expectedMin = ZERO;
        final var expectedCount = 10L;
        Arrays.stream(transactionAmounts)
              .map(amount -> new StatisticsTransaction(new BigDecimal(amount), System.currentTimeMillis()))
              .forEach(transaction -> statsService.add(transaction));
        final var queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(expectedSum, expectedAverage, expectedMax, expectedMin, expectedCount, queryResult);
    }

    @Test
    void testCollectSuccessWithOneThousandTransactions() {
        final var startingAmount = ONE;
        final var incrementAmount = new BigDecimal(".10");
        final var expectedSum = new BigDecimal(51050);
        final var expectedAverage = new BigDecimal("51.05");
        final var expectedMax = new BigDecimal(101);
        final var expectedMin = startingAmount.add(incrementAmount);
        final var expectedCount = 1_000L;
        var amount = startingAmount;
        for (var i = 0; i < expectedCount; i++) {
            amount = amount.add(incrementAmount);
            statsService.add(new StatisticsTransaction(amount, System.currentTimeMillis()));
        }
        final var queryResult = statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE);
        assertSuccessQueryResults(expectedSum, expectedAverage, expectedMax, expectedMin, expectedCount, queryResult);
    }

    @Test
    void testCollectFailsWhenAverageIsNonTerminatingAndRoundingModeIsUnnecessary() {
        addTransactionImpl(new BigDecimal(".25"), 2);  //NOTE: precision=2, scale=2
        addTransactionImpl(new BigDecimal(".50"), 1);
        assertThrows(ArithmeticException.class, () -> {
            statsService.collect(CALC_SCALE, RoundingMode.UNNECESSARY);
        });
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
    void testRemoveExpiredSuccessWithNoTransactions() {
        statsService.removeExpired();
        assertEquals(0L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testRemoveExpiredSuccessWithSingleUnexpiredTransaction() {
        addTransactionImpl(AMOUNT, 1);
        statsService.removeExpired();
        assertEquals(1L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    @Test
    void testRemoveExpiredSuccessWhenSingleTransactionExpires() {
        addTransactionImpl(AMOUNT, 1);
        Awaitility.await().until(() -> {
            return !statsService.hasTransactions();
        });
        statsService.removeExpired();
        assertEquals(0L, statsService.collect(CALC_SCALE, CALC_ROUNDING_MODE).count());
    }

    /**
     * Add one or more transactions
     *
     * @param count The number of transactions to add to the service
     */
    private void addTransactionImpl(final BigDecimal amount, final int count) {
        assert count > 0;
        for (var i = 0; i < count; i++)
            statsService.add(new StatisticsTransaction(amount, System.currentTimeMillis()));
    }
}
