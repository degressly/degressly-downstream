# degressly-downstream
Downstream proxy for Degressly.

[!["Buy Me A Coffee"](https://www.buymeacoffee.com/assets/img/custom_images/orange_img.png)](https://www.buymeacoffee.com/daniyaalk)

---

degressly-downstream tracks downstream requests made by an application under regression testing.
It takes observations of API calls performed by each node(primary, secondary and candidate) and can send data to degressly-comparator for further analysis.

## Quick start

Run degressly-core with:
```mvn spring-boot:run```

### Config flags (VM options)

| VM Options (When running jar)                    | Environment Variables (When using Docker)        | Example                                                                      | Description                                                                                                                                      |
|--------------------------------------------------|--------------------------------------------------|------------------------------------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
| diff.publisher.bootstrap-servers                 | diff_publisher_bootstrap-servers                 | localhost:9092 / kafka:9092                                                  | (Optional) Address of kafka bootstrap servers for integration with degressly-comparator.                                                         |
| diff.publisher.topic-name                        | diff_publisher_topic-name                        | diff_stream                                                                  | (Optional) Kafka topic name for integration with degressly-comparator                                                                            |
| proxy.service.type                               | proxy_service_type                               | IDEMPOTENT_DOWNSTREAM(default), NON_IDEMPOTENT_DOWNSTREAM, REPLAY_DOWNSTREAM | Proxy requests in non-idempotent downstream mode when set to true                                                                                |
| non-idempotent.wait.timeout                      | non-idempotent_wait_timeout                      | 1000000(default)                                                             | Time in ms to wait when a request arrives from secondary or candidate before primary                                                             |
| non-idempotent.wait.retry-interval               | non_idempotent_wait_retry_interval               | 100(default)                                                                 | Interval for performing cache lookups when waiting for response of primary request in non-idempotent mode                                        |
| primary.hostname                                 | primary_hostname                                 | 10.0.0.1                                                                     | Hostname of primary instance, used to infer the origin of an http request.                                                                       |
| secondary.hostname                               | secondary_hostname                               | 10.0.0.2                                                                     | Hostname of secondary instance, used to infer the origin of an http request.                                                                     |
| candidate.hostname                               | candidate_hostname                               | 10.0.0.3                                                                     | Hostname of candidate instance, used to infer the origin of an http request.                                                                     |
| return.response.from                             | return_response_from                             | PRIMARY(default) \| SECONDARY \| CANDIDATE                                   | Which instance's response is to be returned to the application, also determines which downstream's request flows to the upstream service.        |
| degressly.downstream.observation.use.request.uri | degressly_downstream_observation_use_request_uri | true/false(default)                                                          | Only consider request URI instead of full request URL when pushing observations. Set to true if application performs client side load balancing. |
| degressly.downstream.primary.bootstrap-servers   | degressly_downstream_primary_bootstrap_servers   | primary-kafka:9092                                                           | (Optional) Bootstrap servers of primary kafka.                                                                                                   |
| degressly.downstream.secondary.bootstrap-servers | degressly_downstream_secondary_bootstrap_servers | secondary-kafka:9092                                                         | (Optional) Bootstrap servers of secondary kafka.                                                                                                 |
| degressly.downstream.candidate.bootstrap-servers | degressly_downstream_candidate_bootstrap_servers | candidate-kafka:9092                                                         | (Optional) Bootstrap servers of candidate kafka.                                                                                                 |
| degressly.downstream.kafka.consumer.id           | degressly_downstream_kafka_consumer_id           | degressly_downstream_observer(default)                                       | (Optional) Consumer group ID of Kafka diff listener.                                                                                             |

#### Hostname inference
Hostname inference is necessary for determining the source of a request for recording an observation and for caching in case of non-idempotent downstream. 
It can be done by either providing the `primary.hostname`, `secondary.hostname` and `candidate.hostname` values or by adding a `x-degressly-caller` header with the values `PRIMARY`, `SECONDARY` or `CANDIDATE`.

## Modes:
* Idempotent downstream mode:
  * Calls from each instance are proxied to the downstream service and their respective responses returned.
  ![](images/idempotent-request.png)
* Non-idempotent downstream mode:
  * To be used when downstream services must be called once and only once.
  * Downstream response from the primary request is stored in cache, and the same is returned for secondary and candidate API calls for the same URL and trace id.
  ![](images/non-idempotent-request.png)



## Limitations
Some work in progress limitations are listed below:

* Since degressly-downstream works as an HTTP Proxy, HTTPS S2S calls are not supported. 
  * This can be worked around by using an LB that performs SSL termination with a trusted self-signed certificate - left as an exercise for the reader 😉
* New API Integrations cannot be tested when operating in non-idempotent since cache will never be loaded.
* `trace-id` headers are a hard requirement from each client.
#### Good first issues for new contributors:
* Lots of cache interface code is synchronized, causing performance impact.
* There should be configurable fallbacks for non-idempotent downstream proxy. 
  * A flag should allow a request to be served from actual downstream instead of cache after x seconds of waiting.
  * A header in the request should be able to override config and force a request to be fetched from downstream even if non-idempotent proxy is enabled.


## Support

For queries, feature requests and more details on degressly, please visit [github.com/degressly/degressly-core](https://github.com/degressly/degressly-core).