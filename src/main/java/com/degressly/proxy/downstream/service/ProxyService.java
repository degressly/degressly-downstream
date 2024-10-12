package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.RequestContext;
import lombok.Data;
import lombok.Getter;
import org.springframework.http.ResponseEntity;

public interface ProxyService {

	ResponseEntity fetch(final RequestContext requestContext);

	PROXY_SERVICE_TYPE type();

	@Getter
	enum PROXY_SERVICE_TYPE {

		IDEMPOTENT_DOWNSTREAM, NON_IDEMPOTENT_DOWNSTREAM, REPLAY_DOWNSTREAM

	}

}
