package net.sattler22.stats.api;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsService.StatisticsTransaction;
import net.sattler22.stats.util.JsonError;

/**
 * Statistics API REST Controller
 *
 * @author Pete Sattler
 */
@RestController
@RequestMapping("/stats-api")
public class StatisticsRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsRestController.class);
    private StatisticsService statsService;

    @Autowired
    public StatisticsRestController(StatisticsService statsService) {
        super();
        this.statsService = statsService;
    }

    /**
     * Add a new transaction
     */
    @PostMapping(value = "/transactions", consumes = APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addTransaction(StatisticsTransaction transaction) {
        HttpStatus httpStatus = HttpStatus.CREATED;
        try {
            statsService.add(transaction);
        }
        catch(IllegalArgumentException e) {
            httpStatus = HttpStatus.NO_CONTENT;
        }
        return new ResponseEntity<>(httpStatus);
    }

    /**
     * Get statistics based on the transactions which happened in the last 60 seconds
     */
    @GetMapping(value = "/statistics", produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public @ResponseBody StatisticsQueryResult getStatistics() {
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
