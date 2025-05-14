# Spring Boot Real-time Statistics REST API

I first worked on this simple REST API back in July 2018. It was one of the first stand-alone projects that I uploaded to GitHub. I 
updated it to Java 17 and cleaned-up some of the underlying implementation details in March 2022. I continue to enhance it for the 
latest Java LTS releases going forward.

## Getting Started

These instructions will get you a copy of this project up and running on your local machine. Please make sure your 
__JAVA_HOME__ environment variable is set to a valid JDK installation.

1. Clone this Git repository:
```text
git clone https://github.com/peter-sattler/stats-api
```
2. Switch to the application directory:
```text
cd stats-api
```
3. Run the program:
```text
./mvnw spring-boot:run
```

You can then point your browser to the [Swagger UI](http://localhost:8080/swagger-ui/index.html) to interact with the API:

<img src="/images/stats-api-swagger-ui-v2.0.0.png" alt="Swagger UI Image">

## Specifications
 
The main use case for the API is to calculate real-time statistics for the last 60 seconds. There will be two end-points, one 
of them is called every time a transaction is made. It is also the sole input of this REST API. The other one collects 
the statistics based on the transactions in the last 60 seconds.
 
### POST /transactions
 
 * Every time a new transaction occurs, this end-point will be called:

 ```
 Body:
 {
     "amount": 12.3,
     "timestamp": 1478192204000
 }
 ```

 Where:
 
 * _amount_ - is a double specifying the transaction amount
 * _timestamp_ - is a long specifying the transaction time in seconds from the UNIX epoch. It is not the current timestamp.

 Returns an empty body with either:
 
 * 201 - in case of success
 * 409 - if transaction is older than 60 seconds

 ### GET /statistics
 
 This is the main end-point of this task, this end-point have to execute in constant time and memory (O(1)). It returns 
 the following statistics based on the transactions which happened in the last 60 seconds.

Returns:

```
{
    "sum": 1000,
    "avg": 100,
    "max": 200,
    "min": 50,
    "count": 10
}
```

Where:

* _sum_ is a double specifying the total sum of transaction value in the last 60 seconds
* _avg_ is a double specifying the average amount of transaction value in the last 60 seconds
* _max_ is a double specifying single highest transaction value in the last 60 seconds
* _min_ is a double specifying single lowest transaction value in the last 60 seconds
* _count_ is a long specifying the total number of transactions happened in the last 60 seconds

### Other Considerations

For the REST API, the biggest and maybe hardest requirement is to make the __GET /statistics__ execute in constant time 
and space. The best solution would be O(1). It is highly recommended to tackle the O(1) requirement as the last thing 
to do.

Other requirements, which are obvious, but also listed here explicitly:

* The API has to be thread-safe with concurrent requests.  
* The API has to function properly, with proper results.  
* The project should be buildable, and tests should also complete successfully.  
* The API should be able to deal with time discrepancy, which means, at any point of time, we could receive a transaction which have a timestamp of the past.  
* Make sure to send the case in memory solution without database (including in-memory database).  
* End-points have to execute in constant time and memory (O(1)).  

### Version History
* July 2018 (v1.0.0)   
* March 2022 (v1.1.0)
* May 2025 (v2.0.0)

Pete Sattler  
May 2025  
_peter@sattler22.net_  
