FROM maven:3.9.6-amazoncorretto-21

WORKDIR /app

ADD pom.xml .

RUN mvn clean verify --fail-never

ARG diff_publisher_bootstrap_servers
ARG diff_publisher_topic_name=diff_stream
ARG non_idempotent_proxy_enabled=false
ARG non_idempotent_wait_timeout=1000000
ARG non_idempotent_wait_retry_interval=100
ARG primary_hostname
ARG secondary_hostname
ARG candidate_hostname
ARG groovy_downstream_handler=false


ENV diff_publisher_bootstrap_servers=${diff_publisher_bootstrap_servers}
ENV diff_publisher_topic_name=${diff_publisher_topic_name}
ENV non-idempotent_proxy_enabled=${non_idempotent_proxy_enabled}
ENV non-idempotent_wait_timeout=${non_idempotent_wait_timeout}
ENV non_idempotent_wait_retry_interval=${non_idempotent_wait_retry_interval}
ENV primary_hostname=${primary_hostname}
ENV secondary_hostname=${secondary_hostname}
ENV candidate_hostname=${candidate_hostname}
ENV groovy_downstream_handler=${groovy_downstream_handler}


COPY . .
RUN mvn clean package

EXPOSE 8080

ENTRYPOINT ["java", "-jar", \
        "-Ddiff.publisher.bootstrap-servers=${diff_publisher_bootstrap-servers}", \
        "-Ddiff.publisher.topic-name=${diff_publisher_topic-name}", \
        "target/downstream-0.0.1-SNAPSHOT.jar" \
    ]