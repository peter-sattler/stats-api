package net.sattler22.stats.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.servlet.ServletContext;
import net.sattler22.stats.annotation.StatisticsAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Real-Time Statistics API Swagger Configuration
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @since March 2022
 * @version May 2025
 */
@Configuration
public class SwaggerConfig {

    private final SwaggerProperties swaggerProperties;

    SwaggerConfig(SwaggerProperties swaggerProperties) {
        this.swaggerProperties = swaggerProperties;
    }

    @Bean
    public OpenAPI statsOpenAPI(ServletContext servletContext) {
        final Info info = new Info()
                .title(swaggerProperties.info().title())
                .description(swaggerProperties.info().description())
                .version(swaggerProperties.info().version())
                .license(new License()
                        .name(swaggerProperties.info().license())
                        .url(swaggerProperties.info().licenseUrl()));
        final Server serversItem = new Server()
                .url(servletContext.getContextPath())
                .description("Default Server URL");
        return new OpenAPI().info(info).addServersItem(serversItem);
    }

    @Bean
    public GroupedOpenApi statsGroupedOpenApi() {
        return GroupedOpenApi.builder()
                .packagesToScan(swaggerProperties.api().controllerPackage())
                .addOpenApiMethodFilter(method -> method.isAnnotationPresent(StatisticsAPI.class))
                .group(swaggerProperties.api().groupName())
                .build();
    }
}
