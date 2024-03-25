package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.RequestCacheObject;
import com.degressly.proxy.downstream.dto.RequestContext;
import org.springframework.http.ResponseEntity;

public interface RequestCacheService {

	RequestCacheObject storeRequest(RequestContext requestContext);

	RequestCacheObject storeResponse(RequestContext requestContext, ResponseEntity response);

	void fetch();

}
