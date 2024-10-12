package com.degressly.proxy.downstream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownstreamResponse {

	private int statusCode;

	private String body;

	// Headers are modeled as a LinkedMultiValueMap to handle the list of strings
	private Map<String, List<String>> headers;

}