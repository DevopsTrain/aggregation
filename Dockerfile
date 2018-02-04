FROM openjdk:8-jdk-alpine
EXPOSE 8080
VOLUME /tmp

COPY target/*.war /app.war
COPY target/newrelic.jar /newrelic.jar
COPY newrelic/newrelic.yml /newrelic.yml


# default command to run Java application
CMD ["/usr/bin/java", "-javaagent:newrelic.jar", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.war"]
