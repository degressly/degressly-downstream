package com.degressly.proxy.downstream.exception;

public class UnableToResolveIdempotencyDetailsException extends RuntimeException {

	public UnableToResolveIdempotencyDetailsException() {
		super();
	}

	public UnableToResolveIdempotencyDetailsException(Exception e) {
		super(e);
	}

}
