FROM maven:3.5.0-jdk-8 AS builder

# speed up Maven JVM a bit
ENV MAVEN_OPTS="-XX:+TieredCompilation -XX:TieredStopAtLevel=1"

# make source folder
RUN mkdir -p /app
WORKDIR /app

# copy necessary source files (keep code snapshot in image)
COPY pom.xml /app
COPY src/ /app/src/

# run packaging
RUN mvn -B -e package -T 1C

# customize base JDK version here
FROM openjdk:8-jre AS target

EXPOSE 8080

# copy Java application WAR file: package app bytecode and libs into single WAR
COPY --from=builder /app/target/*.war /app.war
COPY --from=builder /app/target/newrelic.jar /newrelic.jar
COPY newrelic/newrelic.yml /newrelic.yml


# default command to run Java application
CMD ["/usr/bin/java", "-javaagent:newrelic.jar", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.war"]