package net.sattler22.stats.producer;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.TEN;
import static java.util.concurrent.TimeUnit.SECONDS;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.sattler22.stats.service.StatisticsService;
import net.sattler22.stats.service.StatisticsService.StatisticsQueryResult;
import net.sattler22.stats.service.StatisticsServiceDelayQueueImpl;
import net.sattler22.stats.service.StatisticsServiceUnitTestHarness;

/**
 * Statistics Producer Unit Test Harness
 *
 * @author Pete Sattler
 */
@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class StatisticsProducerUnitTestHarness {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsProducerUnitTestHarness.class);

    @Autowired
    private StatisticsService statsService;

    @Autowired
    private StatisticsProducerProperties statsProducerProps;

    @Before
    public void resetStatisticsService() {
        statsService.reset();
    }

    @Test
    public void produceOneTransactionTestCase() throws InterruptedException {
        final BigDecimal startingAmount = ONE;
        final BigDecimal incrementAmount = TEN;
        final BigDecimal expectedTotal = startingAmount.add(incrementAmount);
        final StatisticsService statsService = new StatisticsServiceDelayQueueImpl(new DelayQueue<>(), 0);
        final StatisticsProducer statsProducer = new StatisticsProducer(incrementAmount, Duration.ZERO, 10L, statsService);
        statsProducer.produce(startingAmount);
        final StatisticsQueryResult queryResult = statsService.getStatistics();
        StatisticsServiceUnitTestHarness.checkResult(1L, expectedTotal, expectedTotal, expectedTotal, expectedTotal, queryResult);
    }

    @Test
    public void produceMultipleTransactionsTestCase() throws InterruptedException, ExecutionException {
        final BigDecimal incrementAmount = statsProducerProps.getIncrementAmount();
        LOGGER.info("Sleeping so multiple transactions can be created...");
        SECONDS.sleep(20);
        final StatisticsQueryResult queryResult = statsService.getStatistics();
        final long capturedTransactionCount = queryResult.getCount();  //IMPORTANT: Unable to predict, so just copy it!!!
        final BigDecimal expectedSum = incrementAmount.multiply(BigDecimal.valueOf(capturedTransactionCount));
        StatisticsServiceUnitTestHarness.checkResult(capturedTransactionCount, expectedSum,
                                                     incrementAmount, incrementAmount, incrementAmount, queryResult);
    }
}
