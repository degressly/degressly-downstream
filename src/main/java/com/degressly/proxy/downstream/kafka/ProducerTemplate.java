package com.degressly.proxy.downstream.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty("diff.publisher.bootstrap-servers")
public class ProducerTemplate {

	@Value("${diff.publisher.topic-name}")
	private String topicName;

	private final KafkaTemplate<String, String> kafkaTemplate;

	public void sendMessage(String traceId, String msg) {
		kafkaTemplate.send(topicName, traceId, msg);
	}

}
