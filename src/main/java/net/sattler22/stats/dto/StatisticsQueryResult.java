package net.sattler22.stats.dto;

import java.math.BigDecimal;

/**
 * Real-time Statistics Query Result
 *
 * @author Pete Sattler
 * @since July 2018
 * @version November 2025
 */
public record StatisticsQueryResult(BigDecimal sum, BigDecimal avg, BigDecimal max, BigDecimal min, long count) {
}
