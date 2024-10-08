package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import com.degressly.proxy.downstream.service.ProxyCoordinatorService;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.degressly.proxy.downstream.service.factory.ProxyServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Service
public class ProxyCoordinatorServiceImpl implements ProxyCoordinatorService {

	Logger logger = LoggerFactory.getLogger(ProxyCoordinatorServiceImpl.class);

	@Autowired
	ProxyServiceFactory proxyServiceFactory;

	@Autowired
	RequestCacheService requestCacheService;

	@Autowired
	List<ObservationPublisherService> observationPublisherServices;

	ExecutorService observationPublisherExecutorService = Executors.newCachedThreadPool();

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {

		// Update caches
		requestCacheService.storeRequest(requestContext);

		// Proxy request to downstream
		ProxyService proxyService = proxyServiceFactory.getProxyService(requestContext);
		ResponseEntity response = proxyService.fetch(requestContext);

		RequestCacheObject updatedRequestCacheObject = requestCacheService.storeResponse(requestContext, response);
		logger.debug("updatedRequestCacheObject: {}", updatedRequestCacheObject);

		publishObservationIfAllDataIsAvailable(requestContext.getRequest().getRequestURL().toString(),
				updatedRequestCacheObject);

		return response;
	}

	private void publishObservationIfAllDataIsAvailable(String requestUrl,
			RequestCacheObject updatedRequestCacheObject) {
		String traceId = MDC.get(TRACE_ID);

		// synchronized (this) {
		// if (updatedRequestCacheObject.isObservationPublished()) {
		// return;
		// }
		// updatedRequestCacheObject.setObservationPublished(true);
		// }
		//
		// observationPublisherExecutorService.submit(() -> {
		// if (Objects.nonNull(updatedRequestCacheObject.getPrimaryRequest())
		// && Objects.nonNull(updatedRequestCacheObject.getSecondaryRequest())
		// && Objects.nonNull(updatedRequestCacheObject.getCandidateRequest())) {
		//
		// var observation = Observation.builder()
		// .requestUrl(requestUrl)
		// .traceId(traceId)
		// .observationType("REQUEST")
		// .primaryRequest(updatedRequestCacheObject.getPrimaryRequest())
		// .candidateRequest(updatedRequestCacheObject.getCandidateRequest())
		// .secondaryRequest(updatedRequestCacheObject.getSecondaryRequest())
		// .build();
		//
		// observationPublisherServices.forEach((service) ->
		// service.publish(observation));
		// }
		// });

		observationPublisherExecutorService.submit(() -> {
			var observation = Observation.builder()
				.requestUrl(requestUrl)
				.traceId(traceId)
				.observationType("REQUEST")
				.primaryRequest(updatedRequestCacheObject.getPrimaryRequest())
				.candidateRequest(updatedRequestCacheObject.getCandidateRequest())
				.secondaryRequest(updatedRequestCacheObject.getSecondaryRequest())
				.build();

			observationPublisherServices.forEach((service) -> service.publish(observation));
		});

	}

}
