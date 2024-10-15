package com.degressly.proxy.downstream;

import java.util.Set;

public interface Constants {

	String TRACE_ID = "x-degressly-trace-id";

	String CALLER_ID = "x-degressly-caller";

	String DEGRESSLY_CACHE_POPULATION_REQUEST = "degressly-cache-population-request";

	Set<String> HEADERS_TO_SKIP = Set.of("Accept-Encoding", "accept-encoding", "connection", "accept", "Accept",
			"Connection", "content-length", "Content-Length", "transfer-encoding", "host", "Host", "Transfer-Encoding",
			"Keep-Alive", "keep-alive", "Trailer", "trailer", "Upgrade", "upgrade", "Proxy-Authorization",
			"proxy-authorization", "Proxy-Authenticate", "proxy-authenticate");

	enum CALLER {

		PRIMARY, SECONDARY, CANDIDATE

	}

}
