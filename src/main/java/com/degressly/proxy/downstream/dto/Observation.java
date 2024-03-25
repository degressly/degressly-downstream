package com.degressly.proxy.downstream.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Observation {

	String traceId;

	String requestUrl;

	DownstreamResult primaryResult;

	DownstreamResult secondaryResult;

	DownstreamResult candidateResult;

	RequestContext primaryRequest;

	RequestContext secondaryRequest;

	RequestContext candidateRequest;

}
