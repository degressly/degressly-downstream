package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.ObservationPublisherHelper;
import com.degressly.proxy.downstream.service.ProxyCoordinatorService;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.degressly.proxy.downstream.service.factory.ProxyServiceFactory;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

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

	private final ObservationPublisherHelper observationPublisherHelper;

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {

		String observationIdentifier = USE_URI_FOR_OBSERVATION ? requestContext.getRequest().getRequestURI()
				: requestContext.getRequest().getRequestURL().toString();

		// Update caches
		RequestCacheObject updatedRequestCacheObject = requestCacheService.storeRequest(requestContext);
		observationPublisherHelper.publishObservation(observationIdentifier, updatedRequestCacheObject, "REQUEST");

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

}
