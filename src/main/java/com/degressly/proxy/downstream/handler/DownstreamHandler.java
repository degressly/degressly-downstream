package com.degressly.proxy.downstream.handler;

import com.degressly.proxy.downstream.dto.RequestContext;

import java.util.Optional;

public interface DownstreamHandler {

	Optional<Boolean> isIdempotent(RequestContext requestContext);

	Optional<String> getTraceId(RequestContext requestContext);

}
