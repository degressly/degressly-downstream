package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.RequestContext;
import org.springframework.http.ResponseEntity;

public interface ProxyCoordinatorService {

	ResponseEntity fetch(RequestContext requestContext);

}
