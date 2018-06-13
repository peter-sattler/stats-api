package com.sattler.n26;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

/**
 * <h1>N26 Code Challenge 2018</h1>
 * <p/>
 * We would like to have a restful API for our statistics. The main use case for our API is to
 * calculate real-time statistic from the last 60 seconds. There will be two APIs, one of them is
 * called every time a transaction is made. It is also the sole input of this REST API. The other one
 * returns the statistic based of the transactions of the last 60 seconds.
 * <p/>
 * Specs
 * <h3>POST /transactions</h3>
 * <p/>
 * Every time a new transaction happened, this end-point will be called.
 * <pre>
 * Body:
 * {
 *     "amount": 12.3,
 *     "timestamp": 1478192204000
 * }
 * </pre>
 * Where:
 * <ul>
 *   <li>amount - transaction amount</li>
 *   <li>timestamp - transaction time in epoch in millis in UTC time zone (this is not current timestamp)</li>
 * </ul>
 * Returns: Empty body with either 201 or 204.
 * <ul>
 *   <li>201 - in case of success</li>
 *   <li>204 - if transaction is older than 60 seconds</li>
 * </ul>
 * Where:
 * <ul>
 *   <li>amount is a double specifying the amount</li>
 *   <li>time is a long specifying UNIX time format in milliseconds</li>
 * </ul>
 * <h3>GET /statistics</h3>
 * <p/>
 * This is the main end-point of this task, this end-point have to execute in constant time and
 * memory (O(1)). It returns the statistic based on the transactions which happened in the last 60 seconds.
 * <pre>
 * Returns:
 * {
 *     "sum": 1000,
 *     "avg": 100,
 *     "max": 200,
 *     "min": 50,
 *     "count": 10
 * }
 * </pre>
 * Where:
 * <ul>
 *   <li>sum is a double specifying the total sum of transaction value in the last 60 seconds</li>
 *   <li>avg is a double specifying the average amount of transaction value in the last 60 seconds</li>
 *   <li>max is a double specifying single highest transaction value in the last 60 seconds</li>
 *   <li>min is a double specifying single lowest transaction value in the last 60 seconds</li>
 *   <li>count is a long specifying the total number of transactions happened in the last 60 seconds</li>
 * </ul>
 * <h3>Requirements:</h3>
 * <p/>
 * For the REST API, the biggest and maybe hardest requirement is to make the GET /statistics
 * execute in constant time and space. The best solution would be O(1). It is very recommended to
 * tackle the O(1) requirement as the last thing to do as it is not the only thing which will be rated in the code challenge.
 * <p/>
 * Other requirements, which are obvious, but also listed here explicitly:
 * <p/>
 * <ul>
 *   <li>The API have to be thread-safe with concurrent requests</li>
 *   <li>The API have to function properly, with proper result</li>
 *   <li>The project should be buildable, and tests should also complete successfully 
 *       (e.g. If maven is used, then {@code mvn clean install} should complete successfully.</li>
 *   <li>The API should be able to deal with time discrepancy, which means, at any point of time, we could receive a transaction which have a timestamp of the past</li>
 *   <li>Make sure to send the case in memory solution without database (including in-memory database)</li>
 *   <li>End-points have to execute in constant time and memory (O(1))</li>
 *   <li>Please complete the challenge using Java</li>
 * </ul>
 */
@SpringBootApplication
@ComponentScan({ "com.sattler.n26" })
public class N26Application {
    
    public static void main(String[] args) {
        SpringApplication.run(N26Application.class, args);
    }
}
