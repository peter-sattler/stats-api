
# Restful Statistics API 2018

_REST endpoint:_ http://localhost:8080/n26/api/statistics

The main use case for the API is to calculate real-time statistics from the last 60 seconds. There will be two APIs, one of them is
called every time a transaction is made. It is also the sole input of this REST API. The other one returns the statistic based of 
the transactions of the last 60 seconds.

* POST /transactions
* GET /statistics

### Requirements:

* The API have to be thread-safe with concurrent requests
* The API have to function properly, with proper result
* The project should be buildable, and tests should also complete successfully
* The API should be able to deal with time discrepancy, which means, at any point of time, we could receive a transaction which have a timestamp of the past
* Make sure to send the case in memory solution without database (including in-memory database)
* End-points have to execute in constant time and memory (O(1))

Pete Sattler  
July 2018  
_peter@sattler22.net_
