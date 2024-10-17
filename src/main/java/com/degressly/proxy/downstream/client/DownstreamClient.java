package com.degressly.proxy.downstream.client;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@UtilityClass
public class DownstreamClient {

	private final Logger logger = LoggerFactory.getLogger(DownstreamClient.class);

	private static final Set<String> headersToSkip = Set.of("Accept-Encoding", "accept-encoding", "connection",
			"accept", "Accept", "Connection", "content-length", "Content-Length", "transfer-encoding", "host", "Host",
			"Transfer-Encoding", "Keep-Alive", "keep-alive", "Trailer", "trailer", "Upgrade", "upgrade",
			"Proxy-Authorization", "proxy-authorization", "Proxy-Authenticate", "proxy-authenticate");

	public static ResponseEntity getResponse(String host, HttpServletRequest httpServletRequest,
			MultiValueMap<String, String> headers, MultiValueMap<String, String> params, String body) {

		var restTemplate = new RestTemplate();
		var httpEntity = new HttpEntity<>(body, headers);
		var queryParams = new HashMap<String, String>();

		var finalUrl = getFinalUrl(host, httpServletRequest, params, queryParams);

		HttpEntity<String> response;
		MultiValueMap<String, String> newHeaders;

		try {
			response = restTemplate.exchange(finalUrl, HttpMethod.valueOf(httpServletRequest.getMethod()), httpEntity,
					String.class, queryParams);
			newHeaders = filterHeaders(response.getHeaders());

			logger.info("Response for for url {}: Status: {}, Headers: {}, Body: {}", finalUrl, "200",
					response.getHeaders(), response.getBody());

		}
		catch (RestClientResponseException e) {
			logger.info("Response for for url {}: Status: {} Headers: {}, Body: {}", finalUrl, e.getStatusCode(),
					e.getResponseHeaders(), e.getResponseBodyAsString());
			newHeaders = filterHeaders(e.getResponseHeaders());
			return new ResponseEntity(e.getResponseBodyAsString(), newHeaders,
					HttpStatus.valueOf(e.getStatusCode().value()));
		}

		return new ResponseEntity(response.getBody(), newHeaders, HttpStatus.OK);

	}

	private static MultiValueMap<String, String> filterHeaders(@Nullable MultiValueMap<String, String> headers) {
		if (Objects.isNull(headers)) {
			return new LinkedMultiValueMap<>();
		}
		MultiValueMap<String, String> headerMap = new LinkedMultiValueMap<>();
		headers.forEach((key, value) -> {
			if (!headersToSkip.contains(key.toLowerCase())) {
				headerMap.put(key, value);
			}
		});

		return headerMap;
	}

	private static String getFinalUrl(String host, HttpServletRequest httpServletRequest,
			MultiValueMap<String, String> params, Map<String, String> queryParams) {
		UriComponentsBuilder urlTemplate = UriComponentsBuilder.fromHttpUrl(host + httpServletRequest.getRequestURI());

		for (Map.Entry<String, List<String>> entry : params.entrySet()) {
			urlTemplate.queryParam(entry.getKey(), new StringBuilder("{" + entry.getKey() + "}"));
			queryParams.put(entry.getKey(), entry.getValue().getFirst());
		}

		return urlTemplate.build().toUriString();
	}

}
