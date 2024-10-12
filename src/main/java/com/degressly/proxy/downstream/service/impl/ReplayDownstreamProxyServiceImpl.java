package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReplayDownstreamProxyServiceImpl implements ProxyService {

	private final RequestCacheService requestCacheService;

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {
		Optional<RequestCacheObject> requestCacheObject = requestCacheService.fetch(requestContext.getTraceId(),
				requestContext.getIdempotencyKey());

		if (requestCacheObject.isEmpty() || Objects.isNull(requestCacheObject.get().getResponse())) {
			return ResponseEntity.internalServerError()
				.body("Request mapping not present in cache for " + requestContext.getIdempotencyKey());
		}

		DownstreamResponse downstreamResponse = requestCacheObject.get().getResponse();

		return new ResponseEntity(downstreamResponse.getBody(),
				new LinkedMultiValueMap<>(downstreamResponse.getHeaders()), downstreamResponse.getStatusCode());
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.REPLAY_DOWNSTREAM;
	}

}
