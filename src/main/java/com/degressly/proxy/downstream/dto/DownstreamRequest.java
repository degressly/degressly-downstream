package com.degressly.proxy.downstream.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DownstreamRequest {

	Map<String, List<String>> headers;

	Map<String, List<String>> params;

	@Nullable
	String body;

}
