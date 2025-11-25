package net.sattler22.stats.service;

import net.sattler22.stats.dto.StatisticsQueryResult;
import net.sattler22.stats.dto.StatisticsTransaction;

import java.math.RoundingMode;

/**
 * Real-Time Statistics Service
 *
 * @author Pete Sattler
 * @since July 2018
 * @version November 2025
 */
public sealed interface StatisticsService permits StatisticsServiceImpl {

    /**
     * Add a transaction
     *
     * @param transaction A real-time statistics transaction
     */
    void add(StatisticsTransaction transaction);

    /**
     * Transactions existence check
     *
     * @return True if the service has at least one real-time transaction that has not expired. Otherwise, returns false.
     */
    boolean hasTransactions();

    /**
     * Collect statistics
     *
     * @param calcScale The calculation scale (number of digits to the right of the decimal)
     * @param calcRoundingMode The calculation rounding mode
     * @return The statistics based on the real-time transactions which occurred in the last 60 seconds
     */
    StatisticsQueryResult collect(int calcScale, RoundingMode calcRoundingMode);

    /**
     * Remove expired transactions
     */
    void removeIfExpired();
}
