package com.degressly.proxy.downstream.dto;

import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
public class RequestCacheObject {

	RequestContext primaryRequest;

	RequestContext secondaryRequest;

	RequestContext candidateRequest;

	ResponseEntity response; // To be used for non-idempotent requests

}
