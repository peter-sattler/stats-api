package net.sattler22.stats.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import net.jcip.annotations.Immutable;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Real-time Statistics Transaction
 *
 * @author Pete Sattler
 * @since July 2018
 * @version November 2025
 */
@Immutable
public final class StatisticsTransaction {

    private final String id;
    private final BigDecimal amount;
    private final long timestamp;

    /**
     * Constructs a new statistics transaction
     *
     * @param amount The real-time transaction amount
     * @param timestamp The real-time transaction time in seconds from the UNIX epoch
     */
    @JsonCreator
    public StatisticsTransaction(@JsonProperty("amount") BigDecimal amount,
                                 @JsonProperty("timestamp") long timestamp) {
        this.id = UUID.randomUUID().toString();
        this.amount = Objects.requireNonNull(amount, "Amount is required");
        this.timestamp = timestamp;
    }

    public String id() {
        return id;
    }

    public BigDecimal amount() {
        return amount;
    }

    public long timestamp() {
        return timestamp;
    }

    /**
     * Expiration check
     *
     * @param expiryIntervalSecs The real-time transaction expiration interval (in seconds)
     * @return True if the real-time transaction has expired. Otherwise, returns false if it is still active.
     */
    public boolean isExpired(long expiryIntervalSecs) {
        return Instant.now().getEpochSecond() > timestamp + expiryIntervalSecs;
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
        return String.format("%s [id=%s, amount=%s, timestamp=%d]", getClass().getSimpleName(), id, amount, timestamp);
    }
}
