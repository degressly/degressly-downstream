package com.degressly.proxy.downstream;

public interface Constants {

	String TRACE_ID = "trace-id";

	String CALLER_ID = "x-degressly-caller";

	enum CALLER {

		PRIMARY, SECONDARY, CANDIDATE

	}

}
