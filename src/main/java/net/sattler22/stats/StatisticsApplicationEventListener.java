package net.sattler22.stats;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.text.NumberFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import net.sattler22.stats.producer.StatisticsProducer;
import net.sattler22.stats.producer.StatisticsProducerProperties;

/**
 * Statistics Application Event Listener
 *
 * @author Pete Sattler
 */
@Component
public class StatisticsApplicationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(StatisticsApplicationEventListener.class);
    private final StatisticsProducerProperties statsProducerProps;
    private final StatisticsProducer statsProducer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<Integer> producerFuture;         //Only a single thread used

    @Autowired
    public StatisticsApplicationEventListener(StatisticsProducerProperties statsProducerProps, StatisticsProducer statsProducer) {
        super();
        this.statsProducerProps = statsProducerProps;
        this.statsProducer = statsProducer;
    }

    /**
     * Start-up the statistics producer
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws InterruptedException {
        LOGGER.info("Starting statistics producer: [{}]", statsProducer);
        this.producerFuture = executorService.submit(statsProducer);
        int retries = 0;
        while (!statsProducer.hasStarted()) {
            SECONDS.sleep(statsProducerProps.getStartUpCheckIntervalSeconds());
            if (retries > statsProducerProps.getStartUpMaxRetries()) {
                LOGGER.error("Statistics producer failed to start!!!");
                break;
            }
            retries++;
        }
        if (statsProducer.hasStarted()) {
            LOGGER.error("Statistics producer has started successfully");
        }
    }

    /**
     * Properly shutdown the statistics producer
     */
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) throws InterruptedException, ExecutionException {
        LOGGER.info("Gracefully shutting down statistics producer: [{}]", statsProducer);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(statsProducerProps.getShutdownMaxWaitTimeSeconds(), SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        LOGGER.error("Statistics producer shutdown after generating [{}] transactions", NumberFormat.getInstance().format(producerFuture.get()));
    }

    @Override
    public String toString() {
        return String.format("%s [statsProducerProps=%s, statsProducer=%s, executorService=%s, producerFuture=%s]",
                              getClass().getSimpleName(), statsProducerProps, statsProducer, executorService, producerFuture);
    }
}
