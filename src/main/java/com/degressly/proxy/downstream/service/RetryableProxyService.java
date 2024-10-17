package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;
import java.util.Optional;

import static com.degressly.proxy.downstream.Constants.HEADERS_TO_SKIP;

@Slf4j
@RequiredArgsConstructor
public class RetryableProxyService {

	protected final RequestCacheService requestCacheService;

	@Value("${non-idempotent.wait.timeout}")
	private long nonIdempotentWaitTimeout;

	@Value("${non-idempotent.wait.retry-interval}")
	private long nonIdempotentWaitRetryInterval;

	protected ResponseEntity fetchFromCacheWithRetry(RequestContext requestContext) {
		Optional<RequestCacheObject> requestCacheObject = handleRetries(requestContext);

		if (requestCacheObject.isEmpty() || Objects.isNull(requestCacheObject.get().getResponse())) {
			return ResponseEntity.internalServerError()
				.body("Request mapping not present in cache for " + requestContext.getIdempotencyKey());
		}

		DownstreamResponse downstreamResponse = requestCacheObject.get().getResponse();
		log.info("Fetched response: {}", downstreamResponse);

		return new ResponseEntity(downstreamResponse.getBody(), getHeaders(downstreamResponse),
				downstreamResponse.getStatusCode());

	}

	protected Optional<RequestCacheObject> handleRetries(RequestContext requestContext) {
		Optional<RequestCacheObject> requestCacheObject = requestCacheService.fetch(requestContext.getTraceId(),
				requestContext.getIdempotencyKey());
		long timeElapsed = 0;

		while (requestCacheObject.isEmpty() || Objects.isNull(requestCacheObject.get().getResponse())) {
			if (timeElapsed > nonIdempotentWaitTimeout) {
				break;
			}

			try {
				Thread.sleep(nonIdempotentWaitRetryInterval);
				timeElapsed += nonIdempotentWaitRetryInterval;

				requestCacheObject = requestCacheService.fetch(requestContext.getTraceId(),
						requestContext.getIdempotencyKey());

			}
			catch (InterruptedException e) {
				// Do nothing
			}

		}

		return requestCacheObject;
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

}
