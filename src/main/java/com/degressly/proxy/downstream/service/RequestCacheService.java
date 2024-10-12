package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

public interface RequestCacheService {

	RequestCacheObject storeRequest(RequestContext requestContext);

	RequestCacheObject storeResponse(RequestContext requestContext, DownstreamResponse response);

	Optional<RequestCacheObject> fetch(String traceId, String idempotencyKey);

}
