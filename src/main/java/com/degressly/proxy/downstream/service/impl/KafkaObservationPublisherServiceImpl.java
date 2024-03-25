package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.kafka.ProducerTemplate;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty("diff.publisher.bootstrap-servers")
public class KafkaObservationPublisherServiceImpl implements ObservationPublisherService {

	@Autowired
	ProducerTemplate kafkaTemplate;

	Logger logger = LoggerFactory.getLogger(KafkaObservationPublisherServiceImpl.class);

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void publish(Observation result) {
		try {
			Map<String, Object> map = objectMapper.convertValue(result, new TypeReference<>() {
			});
			// parseBodyToJson(map);
			String payload = objectMapper.writeValueAsString(map);
			logger.info("Sending payload {}", payload);
			kafkaTemplate.sendMessage(payload);
		}
		catch (Exception e) {
			logger.error("Error parsing object: {}", result, e);
		}

	}

	@SuppressWarnings("unchecked")
	private void parseBodyToJson(Map<String, Object> map) {
		List<Map<String, Object>> resultMapList = new ArrayList<>(Arrays.asList(
				(Map<String, Object>) map.get("primaryResult"), (Map<String, Object>) map.get("secondaryResult"),
				(Map<String, Object>) map.get("candidateResult")));

		for (Map<String, Object> resultMap : resultMapList) {
			Map<String, Object> httpResponse = (Map<String, Object>) resultMap.get("httpResponse");
			try {
				JsonNode node = objectMapper.readValue((String) httpResponse.get("body"), JsonNode.class);
				httpResponse.put("body", node);
			}
			catch (JsonProcessingException e) {
				// Do nothing
			}
		}

	}

}
