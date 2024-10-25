package com.degressly.proxy.downstream.controller;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import com.degressly.proxy.downstream.service.ProxyCoordinatorService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static com.degressly.proxy.downstream.Constants.DEGRESSLY_CACHE_POPULATION_REQUEST;
import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ProxyController {

	private final ProxyCoordinatorService proxyCoordinatorService;

	private final DownstreamHandlerService downstreamHandlerService;

	private final RequestCacheService requestCacheService;

	private static final ObjectMapper objectMapper = new ObjectMapper();

	@RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.HEAD,
			RequestMethod.OPTIONS, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.TRACE })
	public ResponseEntity proxy(final HttpServletRequest request,
			final @RequestHeader MultiValueMap<String, String> headers,
			final @RequestParam MultiValueMap<String, String> params, final @RequestBody(required = false) String body)
			throws JsonProcessingException {

		log.info("Request received for: {} | Headers: {} | Params: {} | Body: {}", request.getRequestURI(), headers,
				params, body);
		var requestContext = RequestContext.builder()
			.request(request)
			.body(body)
			.headers(headers)
			.params(params)
			.build();

		downstreamHandlerService.populateIdempotencyDetails(requestContext);

		MDC.put(TRACE_ID, requestContext.getTraceId());

		if (isCachePopulationRequest(headers)) {
			var downstreamResponse = objectMapper.readValue(body, DownstreamResponse.class);
			requestCacheService.storeResponse(requestContext, downstreamResponse);
			return ResponseEntity.ok().body("Saved with " + requestContext.getIdempotencyKey());
		}

		return proxyCoordinatorService.fetch(requestContext);
	}

	private static boolean isCachePopulationRequest(MultiValueMap<String, String> headers) {
		return headers.containsKey(DEGRESSLY_CACHE_POPULATION_REQUEST)
				&& Boolean.TRUE.toString().equals(headers.getFirst(DEGRESSLY_CACHE_POPULATION_REQUEST));
	}

}
