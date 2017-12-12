# Aggregation

Aggregates backend status service responses for consumation by various clients. 

## Getting Started

Simple Spring (Java) Project built using Maven. 

### Prerequisites

Build it using Maven 3+. Maven not installed yet? Use the Maven Wrapper (`./mvnw` or `mvnw.cmd`).  
This creates a deployable as well as runnable war file in the target folder.

```
mvn clean install
```

### Configure

see: `src/main/resources/application.properties`

### Running

Deploy to your Java Servlet 3+ container of choice or just directly run the executable war: 

```
java -jar target/aggregation-0.0.1-SNAPSHOT.war
```

To do the same using Docker use the included basic `Dockerfile`. 


Or if a proxy has to be used:

```
java -Dhttp.proxyHost=proxy.domain.xy -Dhttp.proxyPort=3128 -Dhttps.proxyHost=proxy.domain.xy -Dhttps.proxyPort=3128 -Dhttps.proxySet=true -jar target/aggregation-0.0.1-SNAPSHOT.war
```



### The API

GET \<service>/api/vehiclestatus/{vin}


GET \<service>/api/live  
--> HTTP 200