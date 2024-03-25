package com.degressly.proxy.downstream.dto;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;
import org.springframework.util.MultiValueMap;

@Data
@Builder
public class RequestContext {

	final HttpServletRequest request;

	final MultiValueMap<String, String> headers;

	final MultiValueMap<String, String> params;

	@Nullable
	final String body;

}
