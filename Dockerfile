FROM maven:3.9.6-amazoncorretto-21

WORKDIR /app

ADD pom.xml .

RUN mvn clean verify --fail-never

COPY . .
RUN mvn clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", \
        "target/downstream-0.0.1-SNAPSHOT.jar" \
    ]