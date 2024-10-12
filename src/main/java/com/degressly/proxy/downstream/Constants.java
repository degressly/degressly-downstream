package com.degressly.proxy.downstream;

public interface Constants {

	String TRACE_ID = "x-degressly-trace-id";

	String CALLER_ID = "x-degressly-caller";

	String DEGRESSLY_CACHE_POPULATION_REQUEST = "degressly-cache-population-request";

	enum CALLER {

		PRIMARY, SECONDARY, CANDIDATE

	}

}
