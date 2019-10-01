
# Restful Statistics API

_REST Base URI:_ http://localhost:8080/stats-api

The main use case for the API is to calculate real-time statistics from the last 60 seconds. There will be two APIs, one 
of them is called every time a transaction is made. It is also the sole input of this REST API. The other one returns 
the statistic based on the transactions of the last 60 seconds.

 ## Specs
 
 ### POST /transactions
 
 * Every time a new transaction happened, this end-point will be called.

 ```
 Body:
 {
     "amount": 12.3,
     "timestamp": 1478192204000
 }
 ```

 Where:
 
 * amount - is a double specifying the transaction amount
 * timestamp - is a long specifying the transaction time in epoch in milliseconds in UTC time zone (this is not the 
 current timestamp)

 Returns an empty body with either:
 
 * 201 - in case of success
 * 204 - if transaction is older than 60 seconds

 ### GET /statistics
 
 This is the main end-point of this task, this end-point have to execute in constant time and memory (O(1)). It returns 
 the statistic based on the transactions which happened in the last 60 seconds.

```
Returns:
{
    "sum": 1000,
    "avg": 100,
    "max": 200,
    "min": 50,
    "count": 10
}
```

Where:

* sum is a double specifying the total sum of transaction value in the last 60 seconds
* avg is a double specifying the average amount of transaction value in the last 60 seconds
* max is a double specifying single highest transaction value in the last 60 seconds
* min is a double specifying single lowest transaction value in the last 60 seconds
* count is a long specifying the total number of transactions happened in the last 60 seconds

### Requirements:

For the REST API, the biggest and maybe hardest requirement is to make the __GET /statistics__ execute in constant time 
and space. The best solution would be O(1). It is highly recommended to tackle the O(1) requirement as the last thing 
to do.

Other requirements, which are obvious, but also listed here explicitly:

* The API has to be thread-safe with concurrent requests.  
* The API has to function properly, with proper result.  
* The project should be buildable, and tests should also complete successfully.  
* The API should be able to deal with time discrepancy, which means, at any point of time, we could receive a transaction which have a timestamp of the past.  
* Make sure to send the case in memory solution without database (including in-memory database).  
* End-points have to execute in constant time and memory (O(1)).  


Pete Sattler  
July 2018  
_peter@sattler22.net_
