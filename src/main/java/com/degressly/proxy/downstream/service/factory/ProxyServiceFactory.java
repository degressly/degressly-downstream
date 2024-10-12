package com.degressly.proxy.downstream.service.factory;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProxyServiceFactory {

	@Autowired
	private List<ProxyService> proxyServiceList;

	@Value("${proxy.service.type:IDEMPOTENT_DOWNSTREAM}")
	public String proxyServiceType;

	private Map<ProxyService.PROXY_SERVICE_TYPE, ProxyService> serviceMap;

	@PostConstruct
	public void init() {
		Map<ProxyService.PROXY_SERVICE_TYPE, ProxyService> map = new HashMap<>();
		proxyServiceList.forEach((service) -> map.put(service.type(), service));
		serviceMap = Collections.unmodifiableMap(map);
	}

	public ProxyService getProxyService(RequestContext requestContext) {

		if (ProxyService.PROXY_SERVICE_TYPE.REPLAY_DOWNSTREAM.name().equals(proxyServiceType)) {
			return serviceMap.get(ProxyService.PROXY_SERVICE_TYPE.REPLAY_DOWNSTREAM);
		}

		if (requestContext.isIdempotent()) {
			return serviceMap.get(ProxyService.PROXY_SERVICE_TYPE.IDEMPOTENT_DOWNSTREAM);
		}
		return serviceMap.get(ProxyService.PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM);
	}

}
