package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveTraceIdException;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import com.degressly.proxy.downstream.service.ProxyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.degressly.proxy.downstream.Constants.DEGRESSLY_CACHE_POPULATION_REQUEST;
import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Service
public class DefaultDownstreamHandlerServiceImpl implements DownstreamHandlerService {

	@Value("${proxy.service.type:IDEMPOTENT_DOWNSTREAM}")
	private String proxyServiceType;

	@Value("${degressly.downstream.observation.use.request.uri:false}")
	private boolean USE_URI_FOR_OBSERVATION;

	@Override
	public void populateIdempotencyDetails(RequestContext requestContext) {
		requestContext
			.setIdempotent(!ProxyService.PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM.name().equals(proxyServiceType));

		StringBuilder extractedBase = new StringBuilder(USE_URI_FOR_OBSERVATION
				? requestContext.getRequest().getRequestURI() : requestContext.getRequest().getRequestURL().toString());

		if (requestContext.getHeaders().containsKey(TRACE_ID)) {
			requestContext.setTraceId(requestContext.getHeaders().get(TRACE_ID).getFirst());
			requestContext.setIdempotencyKey(extractedBase.append("_").append(requestContext.getTraceId()).toString());
			requestContext.setCachePopulationRequest(
					Boolean.parseBoolean(requestContext.getHeaders().getFirst(DEGRESSLY_CACHE_POPULATION_REQUEST)));
		}
		else {
			throw new UnableToResolveTraceIdException();
		}
	}

}
