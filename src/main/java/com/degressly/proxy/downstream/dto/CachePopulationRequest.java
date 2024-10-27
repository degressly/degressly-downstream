package com.degressly.proxy.downstream.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CachePopulationRequest {

	private String method;

	private String url;

	private int statusCode;

	private Object body;

	// Headers are modeled as a LinkedMultiValueMap to handle the list of strings
	private Map<String, List<String>> headers;

}