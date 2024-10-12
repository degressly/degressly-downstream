package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.CachePopulationRequest;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveTraceIdException;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import com.degressly.proxy.downstream.service.ProxyService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.util.URLEncoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.net.URISyntaxException;

import static com.degressly.proxy.downstream.Constants.DEGRESSLY_CACHE_POPULATION_REQUEST;
import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Service
public class DefaultDownstreamHandlerServiceImpl implements DownstreamHandlerService {

	@Value("${proxy.service.type:IDEMPOTENT_DOWNSTREAM}")
	private String proxyServiceType;

	@Value("${degressly.downstream.observation.use.request.uri:false}")
	private boolean USE_URI_FOR_OBSERVATION;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public void populateIdempotencyDetails(RequestContext requestContext) {
		requestContext
			.setIdempotent(!ProxyService.PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM.name().equals(proxyServiceType));

		boolean isCachePopulationRequest = isCachePopulationRequest(requestContext.getHeaders());

		StringBuilder extractedBase = getExtractedBase(requestContext, isCachePopulationRequest);

		if (!requestContext.getHeaders().containsKey(TRACE_ID)) {
			throw new UnableToResolveTraceIdException();
		}

		requestContext.setTraceId(requestContext.getHeaders().get(TRACE_ID).getFirst());
		requestContext.setIdempotencyKey(extractedBase.append("_").append(requestContext.getTraceId()).toString());
		requestContext.setCachePopulationRequest(isCachePopulationRequest);
	}

	private StringBuilder getExtractedBase(RequestContext requestContext, boolean isCachePopulationRequest) {

		if (isCachePopulationRequest) {

			CachePopulationRequest cachePopulationRequest;
			URI uri;
			try {
				cachePopulationRequest = objectMapper.readValue(requestContext.getBody(), CachePopulationRequest.class);
				uri = new URI(cachePopulationRequest.getUrl());

				return new StringBuilder(
						USE_URI_FOR_OBSERVATION ? new URI(null, null, uri.getPath(), null, uri.getFragment()).toString()
								: new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), null, uri.getFragment())
									.toString());

			}
			catch (URISyntaxException | JsonProcessingException e) {
				throw new RuntimeException(e);
			}
		}

		return new StringBuilder(USE_URI_FOR_OBSERVATION ? requestContext.getRequest().getRequestURI()
				: requestContext.getRequest().getRequestURL().toString());
	}

	private static boolean isCachePopulationRequest(MultiValueMap<String, String> headers) {
		return headers.containsKey(DEGRESSLY_CACHE_POPULATION_REQUEST)
				&& Boolean.TRUE.toString().equals(headers.getFirst(DEGRESSLY_CACHE_POPULATION_REQUEST));
	}

}
