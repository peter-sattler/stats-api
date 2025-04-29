package net.sattler22.stats.config;

import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Real-Time Statistics API Swagger Properties
 *
 * @author Pete Sattler
 * @version May 2025
 */
@ConfigurationProperties(prefix = "springdoc.swagger-ui")
public record SwaggerProperties(Api api, Info info) {

    public record Api(@NotNull String groupName, @NotNull String controllerPackage) {
    }

    public record Info(@NotNull String title, @NotNull String description, @NotNull String license,
                       @NotNull String licenseUrl, @NotNull String version) {
    }
}
