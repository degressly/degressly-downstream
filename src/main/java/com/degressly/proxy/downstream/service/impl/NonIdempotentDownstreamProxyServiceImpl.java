package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.client.DownstreamClient;
import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.RequestHelper;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.degressly.proxy.downstream.service.RetryableProxyService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.Objects;
import java.util.Optional;

@Service
public class NonIdempotentDownstreamProxyServiceImpl extends RetryableProxyService implements ProxyService {

	Logger logger = LoggerFactory.getLogger(NonIdempotentDownstreamProxyServiceImpl.class);

	@Value("${return.response.from:PRIMARY}")
	private String RETURN_RESPONSE_FROM;

	private final RequestHelper requestHelper;

	@Autowired
	public NonIdempotentDownstreamProxyServiceImpl(@Autowired final RequestHelper requestHelper,
			@Autowired final RequestCacheService requestCacheService) {
		super(requestCacheService);
		this.requestHelper = requestHelper;
	}

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {
		Optional<Constants.CALLER> callerOptional = requestHelper.getCaller(requestContext);

		if (callerOptional.isEmpty()) {
			logger.error("Could not resolve caller details, failing.");
			return ResponseEntity.badRequest().build();
		}

		if (callerOptional.get().name().equals(RETURN_RESPONSE_FROM)) {
			logger.info("Sending call to upstream");
			return performDownstreamCall(requestContext);
		}

		logger.info("Caller is not same as RETURN_RESPONSE_FROM, call will be served from cache");
		ResponseEntity responseFromCache = fetchFromCacheWithRetry(requestContext);
		logger.info("Resp: {}", objectMapper.convertValue(responseFromCache, JsonNode.class).toString());

		return responseFromCache;
	}

	private ResponseEntity performDownstreamCall(RequestContext requestContext) {
		HttpServletRequest request = requestContext.getRequest();
		String proto = "http";

		if (StringUtils.isNotBlank(requestContext.getHeaders().getFirst("x-forwarded-proto"))) {
			proto = requestContext.getHeaders().getFirst("x-forwarded-proto");
		}
		String host = proto + "://" + requestContext.getHeaders().getFirst("host");

		ResponseEntity response = DownstreamClient.getResponse(host, request, requestContext.getHeaders(),
				requestContext.getParams(), requestContext.getBody());

		var downstreamResponse = DownstreamResponse.builder()
			.statusCode(response.getStatusCode().value())
			.headers(new LinkedMultiValueMap<>(response.getHeaders()))
			.body(response.getBody() != null ? response.getBody().toString() : null)
			.build();

		requestCacheService.storeResponse(requestContext, downstreamResponse);

		return response;
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM;
	}

}
