package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamResponse;
import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.helper.RequestHelper;
import com.degressly.proxy.downstream.service.ProxyService;
import com.degressly.proxy.downstream.service.RequestCacheService;
import com.degressly.proxy.downstream.service.RetryableProxyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Objects;
import java.util.Optional;

import static com.degressly.proxy.downstream.Constants.HEADERS_TO_SKIP;

@Slf4j
@Service
public class ReplayDownstreamProxyServiceImpl extends RetryableProxyService implements ProxyService {

	@Autowired
	public ReplayDownstreamProxyServiceImpl(@Autowired final RequestCacheService requestCacheService) {
		super(requestCacheService);
	}

	@Override
	public ResponseEntity fetch(RequestContext requestContext) {
		return fetchFromCacheWithRetry(requestContext);
	}

	@Override
	public PROXY_SERVICE_TYPE type() {
		return PROXY_SERVICE_TYPE.REPLAY_DOWNSTREAM;
	}

}
