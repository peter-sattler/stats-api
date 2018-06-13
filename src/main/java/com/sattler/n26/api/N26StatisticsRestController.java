package com.sattler.n26.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.sattler.n26.service.N26StatisticsService;
import com.sattler.n26.service.N26StatisticsService.N26StatisticsQueryResult;
import com.sattler.n26.service.N26StatisticsService.N26StatisticsTransaction;
import com.sattler.n26.util.JsonError;

/**
 * N26 Statistics API REST Controller
 * 
 * @author Pete Sattler
 */
@RestController
@RequestMapping("/n26/api")
public class N26StatisticsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(N26StatisticsRestController.class);
    private N26StatisticsService statsService;

    @Autowired
    public N26StatisticsRestController(N26StatisticsService statsService) {
        super();
        this.statsService = statsService;
    }

    /**
     * Add a new transaction
     */
    @PostMapping(value="/transactions", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addTransaction(N26StatisticsTransaction transaction) {
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            statsService.add(transaction);
        } catch (IllegalArgumentException e) {
            httpStatus = HttpStatus.NO_CONTENT;
        }
        return new ResponseEntity<>(httpStatus);
    }

    /**
     * Get statistics based on the transactions which happened in the last 60 seconds
     */
    @GetMapping(value="/statistics", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody N26StatisticsQueryResult getStatistics() {
        return statsService.getStatistics();
    }

    /**
     * General JSON exception handler
     * 
     * @param exception The troublesome exception
     * @return The error message returned as a JSON error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ModelAndView handleGeneralException(Exception exception) {
        LOGGER.error(exception.getMessage(), exception);
        return new JsonError(exception.getMessage()).asModelAndView();
    }
}
