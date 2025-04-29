package net.sattler22.stats.util;

/**
 * Real-Time Statistics Testing Utilities
 *
 * @author Pete Sattler
 * @version May 2025
 */
public final class TestUtils {

    private TestUtils() {
        throw new AssertionError("Cannot be instantiated");
    }

    /**
     * Get UNIX epoch
     *
     * @return The number of non-leap seconds that have elapsed since 00:00:00 UTC on 1 January 1970
     */
    public static long epoch() {
        return System.currentTimeMillis() / 1000;
    }
}
