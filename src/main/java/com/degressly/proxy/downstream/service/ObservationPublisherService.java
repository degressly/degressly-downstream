package com.degressly.proxy.downstream.service;

import com.degressly.proxy.downstream.dto.Observation;

public interface ObservationPublisherService {

	void publish(Observation result);

}
