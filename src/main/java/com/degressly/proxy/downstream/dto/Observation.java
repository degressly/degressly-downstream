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

	DownstreamRequest primaryRequest;

	DownstreamRequest secondaryRequest;

	DownstreamRequest candidateRequest;

}
