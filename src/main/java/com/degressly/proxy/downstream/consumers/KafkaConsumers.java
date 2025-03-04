package com.degressly.proxy.downstream.consumers;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveTraceIdException;
import com.degressly.proxy.downstream.helper.ObservationPublisherHelper;
import com.degressly.proxy.downstream.service.RequestCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty({ "degressly.downstream.primary.bootstrap-servers",
		"degressly.downstream.secondary.bootstrap-servers", "degressly.downstream.candidate.bootstrap-servers" })
public class KafkaConsumers {

	private final RequestCacheService requestCacheService;

	private final ObservationPublisherHelper observationPublisherHelper;

	@KafkaListener(topicPattern = "${degressly.downstream.kafka.topic.pattern:.*}",
			containerFactory = "primaryKafkaConsumerFactory")
	public void consumePrimaryKafkaMessage(ConsumerRecord<String, String> record) {
		processMessage(record, Constants.CALLER.PRIMARY);
	}

	@KafkaListener(topicPattern = "${degressly.downstream.kafka.topic.pattern:.*}",
			containerFactory = "secondaryKafkaConsumerFactory")
	public void consumeSecondaryKafkaMessage(ConsumerRecord<String, String> record) {
		processMessage(record, Constants.CALLER.SECONDARY);

	}

	@KafkaListener(topicPattern = "${degressly.downstream.kafka.topic.pattern:.*}",
			containerFactory = "candidateKafkaConsumerFactory")
	public void consumeCandidateKafkaMessage(ConsumerRecord<String, String> record) {
		processMessage(record, Constants.CALLER.CANDIDATE);

	}

	private void processMessage(ConsumerRecord<String, String> record, Constants.CALLER callerName) {
		synchronized (this) {
			try {
				log.info(record.key());
				log.info(record.topic());
				log.info(record.value());
				RequestContext requestContext = getRequestContext(record, callerName);
				RequestCacheObject updatedRequestCacheObject = requestCacheService.storeRequest(requestContext);
				observationPublisherHelper.publishObservation(record.topic(), updatedRequestCacheObject, "KAFKA");
			}
			catch (Exception e) {
				log.error("Exception while processing kafka message", e);
			}
		}
	}

	private RequestContext getRequestContext(ConsumerRecord<String, String> record, Constants.CALLER callerName) {
		var requestContext = new RequestContext();

		if (Objects.isNull(record.key())) {
			throw new UnableToResolveTraceIdException();
		}
		MDC.put(TRACE_ID, record.key());
		requestContext.setTraceId(record.key());
		requestContext.setIdempotencyKey(record.topic() + "_" + record.key());
		requestContext.setBody(record.value());
		requestContext.setCallerName(callerName);

		return requestContext;
	}

}
