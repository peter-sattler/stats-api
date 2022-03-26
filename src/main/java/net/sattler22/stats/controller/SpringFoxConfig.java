package net.sattler22.stats.controller;

import static springfox.documentation.service.ApiInfo.DEFAULT;
import static springfox.documentation.spi.DocumentationType.OAS_30;

import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.annotations.Api;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spring.web.plugins.Docket;

/**
 * Real-Time Statistics API SpringFox Configuration
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @version March 2022
 */
@Configuration
public class SpringFoxConfig {

    private static final Logger logger = LoggerFactory.getLogger(SpringFoxConfig.class);
    private final String title;
    private final String description;
    private final String license;
    private final String licenseUrl;
    private final String version;
    private final String host;

    SpringFoxConfig(@Value("#{'${swagger-endpoint.title}'}") String title,
                    @Value("#{'${swagger-endpoint.description}'}") String description,
                    @Value("#{'${swagger-endpoint.license}'}") String license,
                    @Value("#{'${swagger-endpoint.licenseUrl}'}") String licenseUrl,
                    @Value("#{'${swagger-endpoint.version}'}") String version,
                    @Value("#{'${swagger-endpoint.host}'}") String host) {
        this.title = title;
        this.description = description;
        this.license = license;
        this.licenseUrl = licenseUrl;
        this.version = version;
        this.host = host;
    }

    @Bean
    public Docket api(ServletContext servletContext) {
        logger.info("Spring Fox Swagger Host: [{}]", host);
        return new Docket(OAS_30).apiInfo(new ApiInfoBuilder().title(title)
                                                              .description(description)
                                                              .termsOfServiceUrl(DEFAULT.getTermsOfServiceUrl())
                                                              .contact(DEFAULT.getContact())
                                                              .license(license)
                                                              .licenseUrl(licenseUrl)
                                                              .version(version)
                                                              .build())
                                 .select()
                                 .apis(RequestHandlerSelectors.withClassAnnotation(Api.class))
                                 .paths(PathSelectors.any())
                                 .build()
                                 .host(host)
                                 .useDefaultResponseMessages(false);
    }
}
