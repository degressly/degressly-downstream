package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveIdempotencyDetailsException;
import com.degressly.proxy.downstream.handler.DownstreamHandler;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import groovy.lang.GroovyClassLoader;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Objects;

@Slf4j
@Service
@ConditionalOnProperty("groovy.downstream.handler")
public class GroovyDownstreamHandlerServiceImpl implements DownstreamHandlerService {

	@Value("groovy.downstream.handler")
	private String groovyHandlerProperty;

	Class<DownstreamHandler> handlerClass;

	@PostConstruct
	private void init() throws IOException {
		File configScriptFile;
		if (StringUtils.isBlank(groovyHandlerProperty) || Boolean.parseBoolean(groovyHandlerProperty)) {
			ClassLoader classLoader = getClass().getClassLoader();
			configScriptFile = new File(
					Objects.requireNonNull(classLoader.getResource("config/DefaultDownstreamHandler.groovy"))
						.getFile());
		}
		else {
			configScriptFile = new File(groovyHandlerProperty);
		}

		if (!configScriptFile.exists()) {
			throw new FileNotFoundException(configScriptFile.getAbsolutePath());
		}

		try (var classLoader = new GroovyClassLoader()) {
			// noinspection unchecked
			handlerClass = (Class<DownstreamHandler>) classLoader.parseClass(configScriptFile);
		}

	}

	@Override
	public void populateIdempotencyDetails(RequestContext requestContext) {
		try {
			DownstreamHandler handler = handlerClass.getConstructor().newInstance();
			requestContext.setIdempotent(
					handler.isIdempotent(requestContext).orElseThrow(UnableToResolveIdempotencyDetailsException::new));
			requestContext.setTraceId(
					handler.getTraceId(requestContext).orElseThrow(UnableToResolveIdempotencyDetailsException::new));
			requestContext.setTraceId(
					handler.getTraceId(requestContext).orElseThrow(UnableToResolveIdempotencyDetailsException::new));
		}
		catch (Exception e) {
			throw new UnableToResolveIdempotencyDetailsException(e);
		}
	}

}
