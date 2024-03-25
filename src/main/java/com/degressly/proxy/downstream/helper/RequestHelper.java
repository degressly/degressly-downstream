package com.degressly.proxy.downstream.helper;

import com.degressly.proxy.downstream.Constants;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
public class RequestHelper {

	@Value("${primary.hostname:#{null}}")
	private Optional<String> PRIMARY_HOSTNAME;

	@Value("${secondary.hostname:#{null}}")
	private Optional<String> SECONDARY_HOSTNAME;

	@Value("${candidate.hostname:#{null}}")
	private Optional<String> CANDIDATE_HOSTNAME;

	private Map<String, Constants.CALLER> hostnameMap = Collections.emptyMap();

	@PostConstruct
	public void init() {
		if (PRIMARY_HOSTNAME.isPresent() && SECONDARY_HOSTNAME.isPresent() && CANDIDATE_HOSTNAME.isPresent()) {
			this.hostnameMap = Map.of(PRIMARY_HOSTNAME.get(), Constants.CALLER.PRIMARY, SECONDARY_HOSTNAME.get(),
					Constants.CALLER.SECONDARY, CANDIDATE_HOSTNAME.get(), Constants.CALLER.CANDIDATE);
		}
	}

	public Optional<Constants.CALLER> getCaller(HttpServletRequest request) {
		// First priority is given to hostname match, then to headers

		if (!CollectionUtils.isEmpty(hostnameMap)) {
			if (hostnameMap.containsKey(request.getRemoteHost())) {
				return Optional.of(hostnameMap.get(request.getRemoteHost()));
			}
		}

		if (StringUtils.isNotBlank(request.getHeader(Constants.CALLER_ID))) {
			try {
				return Optional.of(Constants.CALLER.valueOf(request.getHeader(Constants.CALLER_ID)));
			}
			catch (Exception e) {
				// Do nothing
			}
		}

		return Optional.empty();

	}

}
