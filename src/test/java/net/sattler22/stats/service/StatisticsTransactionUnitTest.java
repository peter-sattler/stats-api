package net.sattler22.stats.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;

import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;

/**
 * Real-Time Statistics Transaction Unit Test Harness
 *
 * @author Pete Sattler
 * @version March 2022
 */
final class StatisticsTransactionUnitTest {

    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final long EXPIRY_INTERVAL_MILLIS = 60 * 1_000L;
    private static final long TIMESTAMP = System.currentTimeMillis();

    @Test
    void testSuccessHappyPath() {
        final var transaction = new StatisticsTransaction(AMOUNT, TIMESTAMP);
        assertSuccessMandatoryFields(transaction);
        assertFalse(transaction.isExpired(EXPIRY_INTERVAL_MILLIS));
    }

    @Test
    void testSuccessWhenIsExpired() {
        final var transaction = new StatisticsTransaction(AMOUNT, TIMESTAMP);
        assertSuccessMandatoryFields(transaction);
        assertTrue(transaction.isExpired(Math.negateExact(EXPIRY_INTERVAL_MILLIS)));
    }

    @Test
    void testFailsWhenAmountIsNull() {
        assertThrows(NullPointerException.class, () -> {
            new StatisticsTransaction(null, TIMESTAMP);
        });
    }

    private static void assertSuccessMandatoryFields(final StatisticsTransaction transaction) {
        assertNotNull(transaction.id());
        assertEquals(AMOUNT, transaction.amount());
        assertEquals(TIMESTAMP, transaction.timestamp());
    }
}
