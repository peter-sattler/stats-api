package net.sattler22.stats.controller;

import static org.springframework.http.HttpHeaders.LOCATION;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.math.RoundingMode;
import java.net.URI;
import java.util.stream.Collectors;

import javax.validation.constraints.PositiveOrZero;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.ResponseHeader;
import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;

/**
 * Real-Time Statistics API REST Controller
 *
 * @implSpec This class is not designed to be extended, but could not be made final due to Spring's use of the proxy pattern
 * @author Pete Sattler
 * @version July 2018
 * @version March 2022
 */
@Api
@RestController
@RequestMapping("/stats-api/v1")
@Validated
public class StatisticsController {

    private static final int CALC_SCALE_MAX_DIGITS = 9;
    private static final String CALC_SCALE_API_PARAM_DESCRIPTION = "The calculation scale (max " + CALC_SCALE_MAX_DIGITS + " digits to the right of the decimal)";
    private static final String CALC_SCALE_ERROR_MESSAGE = "Calculation scale must be between 0 and " + CALC_SCALE_MAX_DIGITS;
    private final StatisticsService statsService;

    StatisticsController(StatisticsService statsService) {
        this.statsService = statsService;
    }

    /**
     * Add a new transaction
     *
     * @param transaction A real-time statistics transaction
     * @return The HTTP response entity
     */
    @ApiOperation(value = "Add a new transaction")
    @ApiResponses({
        @ApiResponse(code = 201, message = "Transaction added successfully", responseHeaders = {
            @ResponseHeader(name = LOCATION, response = URI.class, description = "Real-time statistics collection URI")
        }),
        @ApiResponse(code = 204, message = "Transaction has expired")
    })
    @PostMapping(value = "/transactions", consumes = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Object> addTransaction(@RequestBody StatisticsTransaction transaction) {
        try {
            statsService.add(transaction);
            final var location = getStatsCollectionUri(ServletUriComponentsBuilder.fromCurrentRequest());
            return ResponseEntity.created(location).build();
        }
        catch(IllegalArgumentException e) {
            return ResponseEntity.noContent().build();
        }
    }

    /**
     * Get real-time statistics collection URI
     *
     * @param uriComponentsBuilder The URI components builder
     * @return The fully-qualified real-time statistics collection URI
     */
    private static URI getStatsCollectionUri(UriComponentsBuilder uriComponentsBuilder) {
        final var pathSegments = uriComponentsBuilder.build().getPathSegments();
        final var replacePath = pathSegments.stream().limit(pathSegments.size() - 1L).collect(Collectors.joining("/"));
        return uriComponentsBuilder.replacePath(replacePath).path("/statistics").build().toUri();
    }

    /**
     * Collect real-time statistics
     *
     * @return The HTTP response entity
     */
    @ApiOperation(value = "Collect real-time statistics")
    @ApiResponses({
        @ApiResponse(code = 200, message = "Real-time statistics collected for all recent transactions"),
        @ApiResponse(code = 400, message = "One or more invalid request parameters found")
    })
    @GetMapping(value = "/statistics", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public StatisticsQueryResult collectStatistics(@ApiParam(value = CALC_SCALE_API_PARAM_DESCRIPTION)
                                                   @RequestParam(defaultValue = "2")
                                                   @PositiveOrZero(message = CALC_SCALE_ERROR_MESSAGE) int calcScale,
                                                   @ApiParam(value = "The calculation rounding mode")
                                                   @RequestParam(defaultValue = "HALF_UP") RoundingMode calcRoundingMode) {
        return statsService.collect(calcScale <= CALC_SCALE_MAX_DIGITS ? calcScale : CALC_SCALE_MAX_DIGITS, calcRoundingMode);
    }
}
