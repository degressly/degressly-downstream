package com.degressly.proxy.downstream.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UtilityClass
public class DownstreamClient {

	private final Logger logger = LoggerFactory.getLogger(DownstreamClient.class);

	public static ResponseEntity getResponse(String host, HttpServletRequest httpServletRequest,
			MultiValueMap<String, String> headers, MultiValueMap<String, String> params, String body) {

		var restTemplate = new RestTemplate();
		var httpEntity = new HttpEntity<>(body, headers);
		var queryParams = new HashMap<String, String>();

		var finalUrl = getFinalUrl(host, httpServletRequest, params, queryParams);

		HttpEntity<String> response;

		try {
			response = restTemplate.exchange(finalUrl, HttpMethod.valueOf(httpServletRequest.getMethod()), httpEntity,
					String.class, queryParams);

			logger.info("Response for for url {}: Status: {}, Headers: {}, Body: {}", finalUrl, "200",
					response.getHeaders(), response.getBody());

		}
		catch (HttpClientErrorException e) {
			logger.info("Response for for url {}: Status: {} Headers: {}, Body: {}", finalUrl, e.getStatusCode(),
					e.getResponseHeaders(), e.getResponseBodyAsString());
			return new ResponseEntity(e.getResponseBodyAsString(), e.getResponseHeaders(),
					HttpStatus.valueOf(e.getStatusCode().value()));
		}

		return new ResponseEntity(response.getBody(), response.getHeaders(), HttpStatus.OK);

	}

	private static String getFinalUrl(String host, HttpServletRequest httpServletRequest,
			MultiValueMap<String, String> params, Map<String, String> queryParams) {
		UriComponentsBuilder urlTemplate = UriComponentsBuilder.fromHttpUrl(host + httpServletRequest.getRequestURI());

		for (Map.Entry<String, List<String>> entry : params.entrySet()) {
			urlTemplate.queryParam(entry.getKey(), new StringBuilder("{" + entry.getKey() + "}"));
			queryParams.put(entry.getKey(), entry.getValue().getFirst());
		}

		urlTemplate = urlTemplate.encode();
		var uriComponents = urlTemplate.buildAndExpand(queryParams);
		var finalUrl = uriComponents.toString();
		return finalUrl;
	}

}
