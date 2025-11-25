package net.sattler22.stats.dto;

import net.sattler22.stats.test.util.TestUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Real-Time Statistics Transaction Unit Tests
 *
 * @author Pete Sattler
 * @since March 2022
 * @version November 2025
 */
final class StatisticsTransactionTest {

    private static final BigDecimal AMOUNT = BigDecimal.ONE;
    private static final long EXPIRY_INTERVAL_SECS = 60;
    private static final long TIMESTAMP = TestUtils.epoch();

    @Test
    void testSuccessHappyPath() {
        final StatisticsTransaction transaction = new StatisticsTransaction(AMOUNT, TIMESTAMP);
        assertSuccessMandatoryFields(transaction);
        assertFalse(transaction.isExpired(EXPIRY_INTERVAL_SECS));
    }

    @Test
    void testSuccessWhenIsExpired() {
        final StatisticsTransaction transaction = new StatisticsTransaction(AMOUNT, TIMESTAMP);
        assertSuccessMandatoryFields(transaction);
        assertTrue(transaction.isExpired(Math.negateExact(EXPIRY_INTERVAL_SECS)));
    }

    @Test
    void testFailsWhenAmountIsNull() {
        assertThrows(NullPointerException.class, () -> new StatisticsTransaction(null, TIMESTAMP));
    }

    private static void assertSuccessMandatoryFields(final StatisticsTransaction transaction) {
        assertNotNull(transaction.id());
        assertEquals(AMOUNT, transaction.amount());
        assertEquals(TIMESTAMP, transaction.timestamp());
    }
}
