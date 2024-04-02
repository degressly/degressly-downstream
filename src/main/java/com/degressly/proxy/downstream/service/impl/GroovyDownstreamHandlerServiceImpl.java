package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.exception.UnableToResolveIdempotencyDetailsException;
import com.degressly.proxy.downstream.exception.UnableToResolveTraceIdException;
import com.degressly.proxy.downstream.handler.DownstreamHandler;
import com.degressly.proxy.downstream.service.DownstreamHandlerService;
import groovy.lang.GroovyClassLoader;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Slf4j
@Service
@Primary
@ConditionalOnProperty("groovy.downstream.handler")
public class GroovyDownstreamHandlerServiceImpl implements DownstreamHandlerService {

	@Value("${groovy.downstream.handler}")
	private String groovyHandlerProperty;

	Class<DownstreamHandler> handlerClass;

	@Autowired
	ResourceLoader resourceLoader;

	@PostConstruct
	private void init() throws IOException {

		InputStream fileInputStream;
		if (StringUtils.isBlank(groovyHandlerProperty) || Boolean.parseBoolean(groovyHandlerProperty)) {
			Resource resource = resourceLoader.getResource("classpath:/config/DefaultDownstreamHandler.groovy");
			fileInputStream = resource.getInputStream();
		}
		else {
			fileInputStream = new FileInputStream(groovyHandlerProperty);
		}

		String groovyScript = new String(fileInputStream.readAllBytes(), StandardCharsets.UTF_8);

		log.info("Loading Downstream Handler: \n {}", groovyScript);

		// if (!configScriptFile.) {
		// log.error("Downstream handler not found");
		// throw new FileNotFoundException(configScriptFile.getAbsolutePath());
		// }

		try (var classLoader = new GroovyClassLoader()) {
			// noinspection unchecked
			handlerClass = (Class<DownstreamHandler>) classLoader.parseClass(groovyScript);
		}

	}

	@Override
	public void populateIdempotencyDetails(RequestContext requestContext) {
		try {
			DownstreamHandler handler = handlerClass.getConstructor().newInstance();
			requestContext.setIdempotent(
					handler.isIdempotent(requestContext).orElseThrow(UnableToResolveIdempotencyDetailsException::new));
			requestContext
				.setTraceId(handler.getTraceId(requestContext).orElseThrow(UnableToResolveTraceIdException::new));

			Optional<String> idempotencyKey = handler.getIdempotencyKey(requestContext);
			if (idempotencyKey.isEmpty() && requestContext.isIdempotent()) {
				throw new UnableToResolveIdempotencyDetailsException();
			}
			requestContext.setIdempotencyKey(idempotencyKey.orElse(null));
		}
		catch (Exception e) {
			throw new UnableToResolveIdempotencyDetailsException(e);
		}
	}

}
