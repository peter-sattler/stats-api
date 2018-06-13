package com.sattler.n26.producer;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Duration;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * N26 Statistics Producer Properties
 * 
 * @author Pete Sattler
 */
public final class N26StatisticsProducerProperties implements Serializable {

    private static final long serialVersionUID = 2921473505035494009L;
    private long startUpCheckIntervalSeconds;
    private int startUpMaxRetries;
    private long shutdownMaxWaitTimeSeconds;
    private BigDecimal incrementAmount;
    private Duration messageDelay;
    private int outOfOrderThreshold;
    private Duration outOfOrderMessageDelayAdjustment;
    private long sleepIntervalSeconds;

    public long getStartUpCheckIntervalSeconds() {
        return startUpCheckIntervalSeconds;
    }

    public void setStartUpCheckIntervalSeconds(long startUpCheckIntervalSeconds) {
        this.startUpCheckIntervalSeconds = startUpCheckIntervalSeconds;
    }

    public int getStartUpMaxRetries() {
        return startUpMaxRetries;
    }

    public void setStartUpMaxRetries(int startUpMaxRetries) {
        this.startUpMaxRetries = startUpMaxRetries;
    }

    public long getShutdownMaxWaitTimeSeconds() {
        return shutdownMaxWaitTimeSeconds;
    }

    public void setShutdownMaxWaitTimeSeconds(long shutdownMaxWaitTimeSeconds) {
        this.shutdownMaxWaitTimeSeconds = shutdownMaxWaitTimeSeconds;
    }

    public BigDecimal getIncrementAmount() {
        return incrementAmount;
    }

    public void setIncrementAmount(BigDecimal incrementAmount) {
        this.incrementAmount = incrementAmount;
    }

    public Duration getMessageDelay() {
        return messageDelay;
    }

    public void setMessageDelaySeconds(long messageDelaySeconds) {
        this.messageDelay = Duration.ofSeconds(messageDelaySeconds);
    }

    public int getOutOfOrderThreshold() {
        return outOfOrderThreshold;
    }

    public void setOutOfOrderThreshold(int outOfOrderThreshold) {
        this.outOfOrderThreshold = outOfOrderThreshold;
    }

    public Duration getOutOfOrderMessageDelayAdjustment() {
        return outOfOrderMessageDelayAdjustment;
    }

    public void setOutOfOrderMessageDelayAdjustment(Duration outOfOrderMessageDelayAdjustment) {
        this.outOfOrderMessageDelayAdjustment = outOfOrderMessageDelayAdjustment;
    }

    public long getSleepIntervalSeconds() {
        return sleepIntervalSeconds;
    }

    public void setSleepIntervalSeconds(long sleepIntervalSeconds) {
        this.sleepIntervalSeconds = sleepIntervalSeconds;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
