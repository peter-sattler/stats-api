package net.sattler22.n26.producer;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import net.sattler22.n26.service.N26StatisticsService;
import net.sattler22.n26.service.N26StatisticsService.N26StatisticsQueryResult;
import net.sattler22.n26.service.N26StatisticsServiceDelayQueueImpl;
import net.sattler22.n26.service.N26StatisticsServiceUnitTestHarness;

/**
 * N26 Statistics Producer Unit Test Harness
 * 
 * @author Pete Sattler
 */
@SpringBootTest
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class N26StatisticsProducerUnitTestHarness {

    private static final Logger LOGGER = LoggerFactory.getLogger(N26StatisticsProducerUnitTestHarness.class);

    @Autowired
    private N26StatisticsService statsService;;

    @Autowired
    private N26StatisticsProducerProperties statsProducerProps;

    @Before
    public void resetStatisticsService() {
        statsService.reset();
    }

    @Test
    public void produceOneTransactionTestCase() throws InterruptedException {
        final BigDecimal startingAmount = BigDecimal.ONE;
        final BigDecimal incrementAmount = BigDecimal.TEN;
        final BigDecimal expectedTotal = startingAmount.add(incrementAmount);
        final N26StatisticsService statsService = new N26StatisticsServiceDelayQueueImpl(new DelayQueue<>(), 0);
        final N26StatisticsProducer statsProducer = new N26StatisticsProducer(incrementAmount, Duration.ZERO, 10L, statsService);
        statsProducer.produce(startingAmount);
        final N26StatisticsQueryResult queryResult = statsService.getStatistics();
        N26StatisticsServiceUnitTestHarness.checkResult(1L, expectedTotal, expectedTotal, expectedTotal, expectedTotal, queryResult);
    }

    @Test
    public void produceMultipleTransactionsTestCase() throws InterruptedException, ExecutionException {
        final BigDecimal incrementAmount = statsProducerProps.getIncrementAmount();
        LOGGER.info("Sleeping so multiple transactions can be created...");
        TimeUnit.SECONDS.sleep(30);
        final N26StatisticsQueryResult queryResult = statsService.getStatistics();
        final long capturedTransactionCount = queryResult.getCount();  // IMPORTANT: Unable to predict, so just copy it!!!
        final BigDecimal expectedSum = incrementAmount.multiply(BigDecimal.valueOf(capturedTransactionCount));
        N26StatisticsServiceUnitTestHarness.checkResult(capturedTransactionCount, expectedSum, incrementAmount, incrementAmount, incrementAmount, queryResult);
    }
}
