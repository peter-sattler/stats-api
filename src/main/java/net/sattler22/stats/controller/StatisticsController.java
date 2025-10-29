package net.sattler22.stats.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.ValidationException;
import net.sattler22.stats.annotation.StatisticsAPI;
import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.RoundingMode;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Real-Time Statistics API REST Controller
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @since July 2018
 * @version May 2025
 */
@RestController
@RequestMapping("/stats-api/v2")
@Validated
public class StatisticsController {

    private static final int MAX_CALC_SCALE = 9;
    private final StatisticsService statisticsService;

    StatisticsController(StatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    /**
     * Add a new transaction
     *
     * @param transaction A real-time statistics transaction
     * @return The HTTP response entity
     */
    @StatisticsAPI
    @Operation(summary = "Add a new transaction")
    @ApiResponse(responseCode = "201", description = "Transaction added successfully")
    @ApiResponse(responseCode = "409", description = "Transaction has expired")
    @PostMapping(value = "/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addTransaction(@RequestBody StatisticsTransaction transaction) {
        statisticsService.add(transaction);
        final URI location = getStatsCollectionUri(ServletUriComponentsBuilder.fromCurrentRequest());
        return ResponseEntity.created(location).build();
    }

    /**
     * Get real-time statistics collection URI
     *
     * @param uriComponentsBuilder The URI components builder
     * @return The fully-qualified real-time statistics collection URI
     */
    private static URI getStatsCollectionUri(UriComponentsBuilder uriComponentsBuilder) {
        final List<String> pathSegments = uriComponentsBuilder.build()
                .getPathSegments();
        final String replacePath = pathSegments.stream()
                .limit(pathSegments.size() - 1L)
                .collect(Collectors.joining("/"));
        return uriComponentsBuilder
                .replacePath(replacePath).path("/statistics")
                .build()
                .toUri();
    }

    /**
     * Collect real-time statistics
     *
     * @return The HTTP response entity
     */
    @StatisticsAPI
    @Operation(summary = "Collect real-time statistics")
    @ApiResponse(responseCode = "200", description = "Real-time statistics collected for all recent transactions")
    @ApiResponse(responseCode = "412", description = "Unable to collect all statistics")
    @ApiResponse(responseCode = "422", description = "One or more invalid request parameters found")
    @GetMapping(value = "/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public StatisticsQueryResult collectStatistics(@Parameter(description = "Calculation scale")
                                                   @RequestParam(defaultValue = "2") int calcScale,
                                                   @Parameter(description = "Calculation rounding mode")
                                                   @RequestParam(defaultValue = "HALF_UP") RoundingMode calcRoundingMode) {
        if (calcScale < 0 || calcScale > MAX_CALC_SCALE)
            throw new ValidationException(String.format("Calculation scale must be between 0 and %d", MAX_CALC_SCALE));
        return statisticsService.collect(calcScale, calcRoundingMode);
    }
}
