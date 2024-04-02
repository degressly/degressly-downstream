package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.client.DownstreamClient;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.RequestHelper;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class NonIdempotentDownstreamProxyServiceImpl implements ProxyService {

	Logger logger = LoggerFactory.getLogger(NonIdempotentDownstreamProxyServiceImpl.class);

	@Value("${non-idempotent.wait.timeout}")
	private long nonIdempotentWaitTimeout;

	@Value("${non-idempotent.wait.retry-interval}")
	private long nonIdempotentWaitRetryInterval;

	@Autowired
	RequestHelper requestHelper;

	@Autowired
	RequestCacheService requestCacheService;

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {
		Optional<Constants.CALLER> callerOptional = requestHelper.getCaller(requestContext);

		if (callerOptional.isEmpty()) {
			logger.error("Could not resolve caller details, failing.");
			return ResponseEntity.badRequest().build();
		}

		if (callerOptional.get().equals(Constants.CALLER.PRIMARY)) {
			return performDownstreamCall(requestContext);
		}

		ResponseEntity responseFromCache = fetchFromCacheWithRetry(requestContext);

		return responseFromCache;
	}

	private ResponseEntity fetchFromCacheWithRetry(RequestContext requestContext) {
		Optional<RequestCacheObject> requestCacheObject = handleRetries(requestContext);

		if (requestCacheObject.isEmpty() || Objects.isNull(requestCacheObject.get().getResponse())) {
			return ResponseEntity.internalServerError().build();
		}

		return requestCacheObject.get().getResponse();
	}

	private Optional<RequestCacheObject> handleRetries(RequestContext requestContext) {
		Optional<RequestCacheObject> requestCacheObject = requestCacheService
			.fetch(requestContext.getRequest().getRequestURL().toString(), requestContext.getTraceId());
		long timeElapsed = 0;

		while (requestCacheObject.isEmpty() || Objects.isNull(requestCacheObject.get().getResponse())) {
			if (timeElapsed > nonIdempotentWaitTimeout) {
				break;
			}

			try {
				Thread.sleep(nonIdempotentWaitRetryInterval);
				timeElapsed += nonIdempotentWaitRetryInterval;

				requestCacheObject = requestCacheService.fetch(requestContext.getRequest().getRequestURL().toString(),
						requestContext.getTraceId());

			}
			catch (InterruptedException e) {
				// Do nothing
			}

		}

		return requestCacheObject;
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

		requestCacheService.storeResponse(requestContext, response);

		return response;
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM;
	}

}
