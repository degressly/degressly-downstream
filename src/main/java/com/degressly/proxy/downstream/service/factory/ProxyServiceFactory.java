package com.degressly.proxy.downstream.service.factory;

import com.degressly.proxy.downstream.dto.RequestContext;
import com.degressly.proxy.downstream.service.ProxyService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProxyServiceFactory {

	@Autowired
	List<ProxyService> proxyServiceList;

	@Value("${non-idempotent.proxy.enabled:false}")
	public String useNonIdemptotentProxy;

	private Map<ProxyService.PROXY_SERVICE_TYPE, ProxyService> serviceMap;

	@PostConstruct
	public void init() {
		Map<ProxyService.PROXY_SERVICE_TYPE, ProxyService> map = new HashMap<>();
		proxyServiceList.forEach((service) -> map.put(service.type(), service));
		serviceMap = Collections.unmodifiableMap(map);
	}

	public ProxyService getProxyService(RequestContext requestContext) {
		if (requestContext.isIdempotent()) {
			return serviceMap.get(ProxyService.PROXY_SERVICE_TYPE.IDEMPOTENT_DOWNSTREAM);
		}
		return serviceMap.get(ProxyService.PROXY_SERVICE_TYPE.NON_IDEMPOTENT_DOWNSTREAM);
	}

}
