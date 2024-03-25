package com.degressly.proxy.downstream.dto;

import lombok.Data;

import java.util.Map;

@Data
public class DownstreamResult {

	private Map<String, Object> httpResponse;

	private Exception exception;

}
