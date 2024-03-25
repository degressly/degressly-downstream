package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.RequestHelper;
import com.degressly.proxy.downstream.service.RequestCacheService;
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

	@Autowired
	RequestHelper requestHelper;

	@Override
	public RequestCacheObject storeRequest(RequestContext requestContext) {
		Optional<Constants.CALLER> caller = requestHelper.getCaller(requestContext.getRequest());

		if (caller.isEmpty()) {
			logger.warn("Could not resolve caller, will not store data in cache");
			return new RequestCacheObject();
		}

		Map<String, RequestCacheObject> traceRequestsMap = cache.getIfPresent(MDC.get(TRACE_ID));

		if (Objects.isNull(traceRequestsMap)) {
			traceRequestsMap = new HashMap<>();
			cache.put(MDC.get(TRACE_ID), traceRequestsMap);
		}

		RequestCacheObject requestsForCurrentUri = traceRequestsMap
			.getOrDefault(requestContext.getRequest().getRequestURI(), new RequestCacheObject());

		switch (caller.get()) {
			case PRIMARY:
				requestsForCurrentUri.setPrimaryRequest(requestContext);
				break;
			case SECONDARY:
				requestsForCurrentUri.setSecondaryRequest(requestContext);
				break;
			case CANDIDATE:
				requestsForCurrentUri.setCandidateRequest(requestContext);
				break;
		}

		traceRequestsMap.put(requestContext.getRequest().getRequestURI(), requestsForCurrentUri);

		return requestsForCurrentUri;

	}

	@Override
	public RequestCacheObject storeResponse(RequestContext requestContext, ResponseEntity response) {
		Map<String, RequestCacheObject> traceRequestsMap = cache.getIfPresent(MDC.get(TRACE_ID));
		Optional<Constants.CALLER> caller = requestHelper.getCaller(requestContext.getRequest());

		if (Objects.isNull(traceRequestsMap)) {
			traceRequestsMap = new HashMap<>();
			cache.put(MDC.get(TRACE_ID), traceRequestsMap);
		}

		RequestCacheObject cacheForCurrentUri = traceRequestsMap
			.getOrDefault(requestContext.getRequest().getRequestURI(), new RequestCacheObject());

		if (caller.isEmpty() || !caller.get().equals(Constants.CALLER.PRIMARY)) {
			return cacheForCurrentUri;
		}

		cacheForCurrentUri.setResponse(response);
		traceRequestsMap.put(requestContext.getRequest().getRequestURI(), cacheForCurrentUri);

		return cacheForCurrentUri;
	}

	@Override
	public void fetch() {
	}

}
