package com.degressly.proxy.downstream.helper;

import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import groovy.util.logging.Slf4j;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ObservationPublisherHelper {

	private final List<ObservationPublisherService> observationPublisherServices;

	private final ExecutorService observationPublisherExecutorService = Executors.newVirtualThreadPerTaskExecutor();

	private Map<ObservationPublisherService, ExecutorService> publisherWiseExecutors;

	@PostConstruct
	public void init() {
		var tempMap = new HashMap<ObservationPublisherService, ExecutorService>();
		// Although messages may be sent to multiple publishers at the same time, each
		// publisher should only deal with one message at a time in sequential order
		// to prevent race conditions.
		observationPublisherServices.forEach(service -> tempMap.put(service, Executors.newSingleThreadExecutor()));
		publisherWiseExecutors = Collections.unmodifiableMap(tempMap);
	}

	public void publishObservation(String requestUrl, RequestCacheObject updatedRequestCacheObject, String type) {
		String traceId = MDC.get(TRACE_ID);

		observationPublisherExecutorService.submit(() -> {
			var observation = Observation.builder()
				.requestUrl(requestUrl)
				.traceId(traceId)
				.observationType(type)
				.primaryRequest(updatedRequestCacheObject.getPrimaryRequest())
				.candidateRequest(updatedRequestCacheObject.getCandidateRequest())
				.secondaryRequest(updatedRequestCacheObject.getSecondaryRequest())
				.build();

			observationPublisherServices
				.forEach(service -> publisherWiseExecutors.get(service).submit(() -> service.publish(observation)));
		});

	}

}
