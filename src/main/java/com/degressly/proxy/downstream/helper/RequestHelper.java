package com.degressly.proxy.downstream.helper;

import com.degressly.proxy.downstream.Constants;
import com.degressly.proxy.downstream.dto.RequestContext;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class RequestHelper {

	@Value("${primary.hostname:#{null}}")
	private String PRIMARY_HOSTNAME;

	@Value("${secondary.hostname:#{null}}")
	private String SECONDARY_HOSTNAME;

	@Value("${candidate.hostname:#{null}}")
	private String CANDIDATE_HOSTNAME;

	private Map<String, Constants.CALLER> hostnameMap = Collections.emptyMap();

	@PostConstruct
	public void init() {
		if (StringUtils.isNotBlank(PRIMARY_HOSTNAME) && StringUtils.isNotBlank(SECONDARY_HOSTNAME)
				&& StringUtils.isNotBlank(CANDIDATE_HOSTNAME)) {
			this.hostnameMap = Map.of(PRIMARY_HOSTNAME, Constants.CALLER.PRIMARY, SECONDARY_HOSTNAME,
					Constants.CALLER.SECONDARY, CANDIDATE_HOSTNAME, Constants.CALLER.CANDIDATE);
		}
	}

	public Optional<Constants.CALLER> getCaller(RequestContext requestContext) {
		// First priority is given to hostname match, then to headers

		if (!CollectionUtils.isEmpty(hostnameMap)) {
			if (hostnameMap.containsKey(requestContext.getRequest().getRemoteHost())) {
				return Optional.of(hostnameMap.get(requestContext.getRequest().getRemoteHost()));
			}
		}

		if (StringUtils.isNotBlank(requestContext.getHeaders().getFirst(Constants.CALLER_ID))) {
			try {
				return Optional.of(Constants.CALLER.valueOf(requestContext.getHeaders().getFirst(Constants.CALLER_ID)));
			}
			catch (Exception e) {
				// Do nothing
			}
		}

		return Optional.empty();

	}

}
