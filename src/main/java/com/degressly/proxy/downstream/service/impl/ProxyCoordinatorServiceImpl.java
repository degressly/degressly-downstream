package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import com.degressly.proxy.downstream.service.ProxyCoordinatorService;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.degressly.proxy.downstream.service.factory.ProxyServiceFactory;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Service
@RequiredArgsConstructor
public class ProxyCoordinatorServiceImpl implements ProxyCoordinatorService {

	Logger logger = LoggerFactory.getLogger(ProxyCoordinatorServiceImpl.class);

	/**
	 * When true, observations will only be pushed with the URI instead of full request
	 * URL. For example, https://test.com/route/to/api will be recorded as /route/tp/api
	 * when true and https://test.com/route/to/api when false. Useful if the application
	 * performs client side load balancing.
	 */
	@Value("${degressly.downstream.observation.use.request.uri:false}")
	private boolean USE_URI_FOR_OBSERVATION;

	private final ProxyServiceFactory proxyServiceFactory;

	private final RequestCacheService requestCacheService;

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

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {

		String observationIdentifier = USE_URI_FOR_OBSERVATION ? requestContext.getRequest().getRequestURI()
				: requestContext.getRequest().getRequestURL().toString();

		// Update caches
		RequestCacheObject updatedRequestCacheObject = requestCacheService.storeRequest(requestContext);
		publishObservation(observationIdentifier, updatedRequestCacheObject);

		// Proxy request to downstream
		ProxyService proxyService = proxyServiceFactory.getProxyService(requestContext);
		ResponseEntity response;
		response = proxyService.fetch(requestContext);

		var downstreamResponse = DownstreamResponse.builder()
			.statusCode(response.getStatusCode().value())
			.headers(new LinkedMultiValueMap<>(response.getHeaders()))
			.body(response.getBody() != null ? response.getBody() : null)
			.build();

		updatedRequestCacheObject = requestCacheService.storeResponse(requestContext, downstreamResponse);
		logger.debug("updatedRequestCacheObject: {}", updatedRequestCacheObject);

		return response;
	}

	private void publishObservation(String requestUrl, RequestCacheObject updatedRequestCacheObject) {
		String traceId = MDC.get(TRACE_ID);

		observationPublisherExecutorService.submit(() -> {
			var observation = Observation.builder()
				.requestUrl(requestUrl)
				.traceId(traceId)
				.observationType("REQUEST")
				.primaryRequest(updatedRequestCacheObject.getPrimaryRequest())
				.candidateRequest(updatedRequestCacheObject.getCandidateRequest())
				.secondaryRequest(updatedRequestCacheObject.getSecondaryRequest())
				.build();

			/*
			 * see comment in
			 * com.degressly.proxy.downstream.service.impl.ProxyCoordinatorServiceImpl.
			 * init
			 */
			observationPublisherServices
				.forEach(service -> publisherWiseExecutors.get(service).submit(() -> service.publish(observation)));
		});

	}

}
