package com.degressly.proxy.downstream.dto;

import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class RequestCacheObject {

	DownstreamRequest primaryRequest;

	DownstreamRequest secondaryRequest;

	DownstreamRequest candidateRequest;

	ResponseEntity response; // To be used for non-idempotent requests

	boolean observationPublished = false;

}
