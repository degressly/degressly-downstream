package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveTraceIdException;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.degressly.proxy.downstream.Constants.TRACE_ID;

@Slf4j
@Service
public class DefaultDownstreamHandlerServiceImpl implements DownstreamHandlerService {

	@Value("${non-idempotent.proxy.enabled:false}")
	public String useNonIdemptotentProxy;

	@Override
	public void populateIdempotencyDetails(RequestContext requestContext) {
		requestContext.setIdempotent(!Boolean.parseBoolean(useNonIdemptotentProxy));

		if (requestContext.getHeaders().containsKey(TRACE_ID)) {
			requestContext.setTraceId(requestContext.getHeaders().get(TRACE_ID).getFirst());
			requestContext.setIdempotencyKey(requestContext.getRequest()
				.getRequestURL()
				.append("_")
				.append(requestContext.getTraceId())
				.toString());
		}
		else {
			throw new UnableToResolveTraceIdException();
		}
	}

}
