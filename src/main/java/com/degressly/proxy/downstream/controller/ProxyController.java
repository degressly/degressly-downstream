package com.degressly.proxy.downstream.controller;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import com.degressly.proxy.downstream.service.ProxyCoordinatorService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Controller
public class ProxyController {

	@Autowired
	ProxyCoordinatorService proxyCoordinatorService;

	@Autowired
	DownstreamHandlerService downstreamHandlerService;

	@RequestMapping(value = "/**", method = { RequestMethod.GET, RequestMethod.POST, RequestMethod.HEAD,
			RequestMethod.OPTIONS, RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.DELETE, RequestMethod.TRACE })
	public ResponseEntity proxy(final HttpServletRequest request,
			final @RequestHeader MultiValueMap<String, String> headers,
			final @RequestParam MultiValueMap<String, String> params,
			final @RequestBody(required = false) String body) {

		var requestContext = RequestContext.builder()
			.request(request)
			.body(body)
			.headers(headers)
			.params(params)
			.build();

		downstreamHandlerService.populateIdempotencyDetails(requestContext);

		MDC.put(TRACE_ID, requestContext.getTraceId());

		return proxyCoordinatorService.fetch(requestContext);
	}

}
