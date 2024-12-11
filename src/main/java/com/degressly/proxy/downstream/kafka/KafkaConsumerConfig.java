package com.degressly.proxy.downstream.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@EnableKafka
@Configuration
@ConditionalOnProperty({ "degressly.downstream.primary.bootstrap-servers",
		"degressly.downstream.secondary.bootstrap-servers", "degressly.downstream.candidate.bootstrap-servers" })
public class KafkaConsumerConfig {

	@Value("${degressly.downstream.primary.bootstrap-servers}")
	private String primaryBootstrapServers;

	@Value("${degressly.downstream.secondary.bootstrap-servers}")
	private String secondaryBootstrapServers;

	@Value("${degressly.downstream.candidate.bootstrap-servers}")
	private String candidateBootstrapServers;

	@Value("${degressly.downstream.kafka.consumer.id:degressly_downstream_observer}")
	private String consumerGroupId;

	public ConsumerFactory<String, String> getConsumerFactory(String bootstrapAddress) {
		log.info("Bootstrap server: {}", bootstrapAddress);
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapAddress);
		props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(props);
	}

	@Bean("primaryKafkaConsumerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, String> primaryKafkaConsumerFactory() {
		var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
		factory.setConsumerFactory(getConsumerFactory(primaryBootstrapServers));
		return factory;
	}

	@Bean("secondaryKafkaConsumerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, String> secondaryKafkaConsumerFactory() {
		var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
		factory.setConsumerFactory(getConsumerFactory(secondaryBootstrapServers));
		return factory;
	}

	@Bean("candidateKafkaConsumerFactory")
	public ConcurrentKafkaListenerContainerFactory<String, String> candidateKafkaConsumerFactory() {
		var factory = new ConcurrentKafkaListenerContainerFactory<String, String>();
		factory.setConsumerFactory(getConsumerFactory(candidateBootstrapServers));
		return factory;
	}

}
