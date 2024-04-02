package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.RequestContext;

public interface DownstreamHandlerService {

	void populateIdempotencyDetails(RequestContext requestContext);

}
