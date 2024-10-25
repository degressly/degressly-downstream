package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.kafka.ProducerTemplate;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty("diff.publisher.bootstrap-servers")
public class KafkaObservationPublisherServiceImpl implements ObservationPublisherService {

	private final ProducerTemplate kafkaTemplate;

	private final Logger logger = LoggerFactory.getLogger(KafkaObservationPublisherServiceImpl.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	private static final ExecutorService singleThreadedExecutorService = Executors.newSingleThreadExecutor();

	@Override
	public void publish(Observation result) {
		// Execute only one at a time to prevent newer, more complete observations being
		// overridden
		singleThreadedExecutorService.submit(() -> {
			try {
				Map<String, Object> map = objectMapper.convertValue(result, new TypeReference<>() {
				});
				String payload = objectMapper.writeValueAsString(map);
				logger.info("Sending payload {}", payload);
				kafkaTemplate.sendMessage(payload);
			}
			catch (Exception e) {
				logger.error("Error parsing object: {}", result, e);
			}
		});
	}

}
