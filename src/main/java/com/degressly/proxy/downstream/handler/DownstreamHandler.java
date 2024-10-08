package com.degressly.proxy.downstream.handler;

import com.degressly.proxy.downstream.dto.RequestContext;

import java.util.Optional;

public interface DownstreamHandler {

	/**
	 * Returns Boolean.TRUE or Boolean.FALSE, depending on whether the current request is
	 * idempotent or non-idempotent. If this returns true, the
	 * {@link DownstreamHandler#getIdempotencyKey(RequestContext)} method will not be
	 * called
	 * @param requestContext Context of the request containing Headers, params and body.
	 * @return Optional boolean true/false depending on the nature of API.
	 */
	Optional<Boolean> isIdempotent(RequestContext requestContext);

	/**
	 * Return the trace ID of the request, given the request context.
	 * @param requestContext Context of the request containing Headers, params and body.
	 * @return Optional string containing the traceId of the API.
	 */
	Optional<String> getTraceId(RequestContext requestContext);

	/**
	 * Return the idempotency key of the request, given the request context. The
	 * idempotency key must be the same for the same flow's call from primary, secondary
	 * and candidate replicas. Based on the idempotency key and the value of
	 * `return.response.from`, only one request will flow to the upstream while the rest
	 * will get a cached copy of the response.
	 * @param requestContext Context of the request containing Headers, params and body.
	 * @return Optional string containing the traceId of the API.
	 */
	Optional<String> getIdempotencyKey(RequestContext requestContext);

}
