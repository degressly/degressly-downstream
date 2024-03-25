package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.RequestContext;
import org.springframework.http.ResponseEntity;

public interface ProxyService {

	ResponseEntity fetch(final RequestContext requestContext);

	PROXY_SERVICE_TYPE type();

	enum PROXY_SERVICE_TYPE {

		IDEMPOTENT_DOWNSTREAM

	}

}
