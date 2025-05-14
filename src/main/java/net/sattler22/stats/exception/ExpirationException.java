package net.sattler22.stats.exception;

import java.io.Serial;

/**
 * Real-Time Statistics Expiration Exception
 *
 * @author Pete Sattler
 * @since May 2025
 */
public final class ExpirationException extends IllegalStateException {

    @Serial
    private static final long serialVersionUID = 1949619734225875437L;

    public ExpirationException(String message) {
        super(message);
    }
}
