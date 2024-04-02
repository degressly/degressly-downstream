package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.client.DownstreamClient;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class IdempotentDownstreamProxyServiceImpl implements ProxyService {

	@Override
	public ResponseEntity fetch(final RequestContext requestContext) {

		HttpServletRequest request = requestContext.getRequest();
		String proto = "http";

		if (requestContext.getHeaders().containsKey("x-forwarded-proto")) {
			proto = requestContext.getHeaders().getFirst("x-forwarded-proto");
		}
		String host = proto + "://" + requestContext.getHeaders().getFirst("host");

		return DownstreamClient.getResponse(host, request, requestContext.getHeaders(), requestContext.getParams(),
				requestContext.getBody());
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.IDEMPOTENT_DOWNSTREAM;
	}

}
