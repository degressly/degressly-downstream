package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;
import java.util.Optional;

import static com.degressly.proxy.downstream.Constants.HEADERS_TO_SKIP;

@Service
@Slf4j
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
		log.info("Fetched response: {}", downstreamResponse);

		return new ResponseEntity(downstreamResponse.getBody(), getHeaders(downstreamResponse),
				downstreamResponse.getStatusCode());
	}

	private static MultiValueMap<String, String> getHeaders(DownstreamResponse downstreamResponse) {

		MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();

		downstreamResponse.getHeaders().forEach((k, v) -> {
			if (!HEADERS_TO_SKIP.contains(k)) {
				headers.put(k, v);
			}
		});

		return headers;
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.REPLAY_DOWNSTREAM;
	}

}
