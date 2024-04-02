package com.degressly.proxy.downstream.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestContext {

	HttpServletRequest request;

	MultiValueMap<String, String> headers;

	MultiValueMap<String, String> params;

	@Nullable
	String body;

	String traceId;

	String idempotencyKey;

	boolean isIdempotent;

}
