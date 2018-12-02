package net.sattler22.n26;

import java.text.NumberFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import net.sattler22.n26.producer.N26StatisticsProducer;
import net.sattler22.n26.producer.N26StatisticsProducerProperties;

/**
 * N26 Statistics Application Event Listener
 * 
 * @author Pete Sattler
 */
@Component
public class N26ApplicationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(N26ApplicationEventListener.class);
    private final N26StatisticsProducerProperties statsProducerProps;
    private final N26StatisticsProducer statsProducer;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Future<Integer> producerFuture;                   // Only a single thread used

    @Autowired
    public N26ApplicationEventListener(N26StatisticsProducerProperties statsProducerProps, N26StatisticsProducer statsProducer) {
        super();
        this.statsProducerProps = statsProducerProps;
        this.statsProducer = statsProducer;
    }

    /**
     * Start-up the N26 statistics producer
     */
    @EventListener
    public void onApplicationEvent(ContextRefreshedEvent event) throws InterruptedException {
        LOGGER.info("Starting N26 statistics producer: [{}]", statsProducer);
        this.producerFuture = executorService.submit(statsProducer);
        int retries = 0;
        while (!statsProducer.hasStarted()) {
            TimeUnit.SECONDS.sleep(statsProducerProps.getStartUpCheckIntervalSeconds());
            if (retries > statsProducerProps.getStartUpMaxRetries()) {
                LOGGER.error("N26 statistics producer failed to start!!!");
                break;
            }
            retries++;
        }
        if (statsProducer.hasStarted()) {
            LOGGER.error("N26 statistics producer has started successfully");
        }
    }

    /**
     * Properly shutdown the N26 statistics producer
     */
    @EventListener
    public void onApplicationEvent(ContextClosedEvent event) throws InterruptedException, ExecutionException {
        LOGGER.info("Gracefully shutting down  N26 statistics producer: [{}]", statsProducer);
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(statsProducerProps.getShutdownMaxWaitTimeSeconds(), TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
        LOGGER.error("N26 statistics producer shutdown after generating [{}] transactions", NumberFormat.getInstance().format(producerFuture.get()));
    }

    @Override
    public String toString() {
        return String.format("%s [statsProducerProps=%s, statsProducer=%s, executorService=%s, producerFuture=%s]", getClass().getSimpleName(), statsProducerProps, statsProducer, executorService, producerFuture);
    }
}
