package net.sattler22.stats.service;

import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import net.jcip.annotations.Immutable;

/**
 * Statistics Service
 *
 * @author Pete Sattler
 */
public interface StatisticsService {

    /**
     * Add a transaction
     *
     * @param transaction The statistics transaction record
     * @throws IllegalArgumentException When the delay exceeds the maximum amount
     */
    void add(StatisticsTransaction transaction) throws IllegalArgumentException;

    /**
     * Transaction existence check
     *
     * @return True if the service has at least one transaction (whether immediately
     *         consumable or not). Otherwise, returns false.
     */
    boolean hasTransactions();

    /**
     * Get statistics
     *
     * @return The statistics based on the transactions which occurred in the last 60 seconds
     */
    StatisticsQueryResult getStatistics();

    /**
     * Reset the service by removing all transactions
     */
    void reset();

    /**
     * Statistics Transaction Record
     */
    @Immutable
    public final class StatisticsTransaction implements Serializable, Delayed {

        private static final long serialVersionUID = 1384041813061895431L;
        private final String id;
        private final BigDecimal amount;
        private final Instant availabileTime;

        /**
         * Constructs a new statistics transaction record
         *
         * @param amount The transaction amount
         * @param delay The delay amount before which the transaction is available for consumption
         */
        public StatisticsTransaction(BigDecimal amount, Duration delay) {
            super();
            this.id = UUID.randomUUID().toString();
            this.amount = amount;
            this.availabileTime = Instant.now().plus(delay);
        }

        public BigDecimal getAmount() {
            return amount;
        }

        @Override
        public int compareTo(Delayed other) {
            //Keep sorted by availability time:
            //NOTE: HEAD is first item to become available for consumption and
            //      the TAIL has the longest time to wait to be consumed
            final StatisticsTransaction that = (StatisticsTransaction) other;
            return this.availabileTime.compareTo(that.availabileTime);

        }

        @Override
        public long getDelay(TimeUnit timeUnit) {
            final Duration remainingDelay = Duration.between(Instant.now(), availabileTime);
            return timeUnit.convert(remainingDelay.toMillis(), MILLISECONDS);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.id);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null)
                return false;
            if (this.getClass() != other.getClass())
                return false;
            final StatisticsTransaction that = (StatisticsTransaction) other;
            return Objects.equals(this.id, that.id);
        }

        @Override
        public String toString() {
            return String.format("%s [id=%s, amount=%s, availabileTime=%s]",
                                  getClass().getSimpleName(), id, amount, availabileTime);
        }
    }

    /**
     * Statistics Query Result
     */
    @Immutable
    public final class StatisticsQueryResult implements Serializable {

        private static final long serialVersionUID = -4750514593176953262L;
        private final BigDecimal sum;
        private final long count;
        private final BigDecimal min;
        private final BigDecimal max;
        private final Instant timestamp = Instant.now();

        /**
         * Constructs a new statistics query result
         *
         * @param sum The sum of the transaction amounts
         * @param count The number of transactions
         * @param min The minimum transaction amount
         * @param max The maximum transaction amount
         */
        public StatisticsQueryResult(BigDecimal sum, long count, BigDecimal min, BigDecimal max) {
            this.sum = sum;
            this.count = count;
            this.min = min;
            this.max = max;
        }

        public BigDecimal getSum() {
            return sum;
        }

        public long getCount() {
            return count;
        }

        /**
         * Calculates the average
         *
         * @param scale The scale of the {@code BigDecimal} to be returned
         * @param roundingMode The rounding mode to apply
         * @return The statistical average or {@code BigDecimal.ZERO} if the count is zero
         */
        public BigDecimal calculateAverage(int scale, RoundingMode roundingMode) {
            if (count == 0) {
                return ZERO;
            }
            return sum.divide(BigDecimal.valueOf(count), scale, roundingMode);
        }

        public BigDecimal getMin() {
            return min;
        }

        public BigDecimal getMax() {
            return max;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.timestamp);
        }

        @Override
        public boolean equals(Object other) {
            if (this == other)
                return true;
            if (other == null)
                return false;
            if (this.getClass() != other.getClass())
                return false;
            final StatisticsQueryResult that = (StatisticsQueryResult) other;
            return Objects.equals(this.timestamp, that.timestamp);
        }

        @Override
        public String toString() {
            return String.format("%s [sum=%s, count=%s, min=%s, max=%s, timestamp=%s]",
                                  getClass().getSimpleName(), sum, count, min, max, timestamp);
        }
    }
}
