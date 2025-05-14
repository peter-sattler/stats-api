package net.sattler22.stats.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Real-Time Statistics API Marker Annotation
 *
 * @author Pete Sattler
 * @since May 2025
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
public @interface StatisticsAPI {
}
