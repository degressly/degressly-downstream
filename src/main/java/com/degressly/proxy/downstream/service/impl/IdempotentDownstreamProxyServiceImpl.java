package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.client.DownstreamClient;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class IdempotentDownstreamProxyServiceImpl implements ProxyService {

	@Override
	public ResponseEntity fetch(final RequestContext requestContext) {

		HttpServletRequest request = requestContext.getRequest();
		String proto = "http";

		if (StringUtils.isNotBlank(request.getHeader("x-forwarded-proto"))) {
			proto = request.getHeader("x-forwarded-proto");
		}
		String host = proto + "://" + request.getHeader("host");

		return DownstreamClient.getResponse(host, request, requestContext.getHeaders(), requestContext.getParams(),
				requestContext.getBody());
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.IDEMPOTENT_DOWNSTREAM;
	}

}
