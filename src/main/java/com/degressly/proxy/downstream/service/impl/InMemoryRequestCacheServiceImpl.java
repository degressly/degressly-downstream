package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.dto.DownstreamRequest;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.RequestHelper;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Service
public class InMemoryRequestCacheServiceImpl implements RequestCacheService {

	Logger logger = LoggerFactory.getLogger(InMemoryRequestCacheServiceImpl.class);

	private final Cache<String, Map<String, RequestCacheObject>> cache = CacheBuilder.newBuilder()
		.expireAfterWrite(1, TimeUnit.HOURS)
		.build();

	private final ExecutorService observationPublisherExecutorService = Executors.newCachedThreadPool();

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	RequestHelper requestHelper;

	@Override
	public RequestCacheObject storeRequest(RequestContext requestContext) {
		Optional<Constants.CALLER> caller = requestHelper.getCaller(requestContext.getRequest());

		if (caller.isEmpty()) {
			logger.warn("Could not resolve caller, will not store data in cache");
			return new RequestCacheObject();
		}

		RequestCacheObject requestsForCurrentUri;
		synchronized (this) {
			Map<String, RequestCacheObject> traceRequestsMap = cache.getIfPresent(MDC.get(TRACE_ID));
			if (Objects.isNull(traceRequestsMap)) {
				traceRequestsMap = new ConcurrentHashMap<>();
				cache.put(MDC.get(TRACE_ID), traceRequestsMap);
			}

			requestsForCurrentUri = traceRequestsMap
					.get(requestContext.getRequest().getRequestURI());
			if (Objects.isNull(requestsForCurrentUri)) {
				requestsForCurrentUri = new RequestCacheObject();
				traceRequestsMap.put(requestContext.getRequest().getRequestURI(), requestsForCurrentUri);
			}
		}


		var downstreamRequest = DownstreamRequest.builder()
			.headers(new HashMap<>(requestContext.getHeaders()))
			.params(requestContext.getParams())
			.body(requestContext.getBody())
			.build();

		switch (caller.get()) {
			case PRIMARY:
				requestsForCurrentUri.setPrimaryRequest(downstreamRequest);
				break;
			case SECONDARY:
				requestsForCurrentUri.setSecondaryRequest(downstreamRequest);
				break;
			case CANDIDATE:
				requestsForCurrentUri.setCandidateRequest(downstreamRequest);
				break;
		}

		return requestsForCurrentUri;

	}

	@Override
	public RequestCacheObject storeResponse(RequestContext requestContext, ResponseEntity response) {
		Optional<Constants.CALLER> caller = requestHelper.getCaller(requestContext.getRequest());


		RequestCacheObject requestsForCurrentUri;
		synchronized (this) {
			Map<String, RequestCacheObject> traceRequestsMap = cache.getIfPresent(MDC.get(TRACE_ID));
			if (Objects.isNull(traceRequestsMap)) {
				traceRequestsMap = new ConcurrentHashMap<>();
				cache.put(MDC.get(TRACE_ID), traceRequestsMap);
			}

			requestsForCurrentUri = traceRequestsMap
					.get(requestContext.getRequest().getRequestURI());
			if (Objects.isNull(requestsForCurrentUri)) {
				requestsForCurrentUri = new RequestCacheObject();
				traceRequestsMap.put(requestContext.getRequest().getRequestURI(), requestsForCurrentUri);
			}
		}

		if (caller.isEmpty() || !caller.get().equals(Constants.CALLER.PRIMARY)) {
			return requestsForCurrentUri;
		}

		requestsForCurrentUri.setResponse(response);

		return requestsForCurrentUri;
	}

	@Override
	public Optional<RequestCacheObject> fetch(String url) {
		Map<String, RequestCacheObject> traceRequestsMap = cache.getIfPresent(MDC.get(TRACE_ID));

		if (Objects.isNull(traceRequestsMap)) {
			return Optional.empty();
		}

		return Optional.ofNullable(traceRequestsMap.get(url));
	}

}
