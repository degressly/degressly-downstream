package com.degressly.proxy.downstream.service.impl;

import com.degressly.proxy.downstream.dto.DownstreamRequest;
import com.degressly.proxy.downstream.dto.Observation;
import com.degressly.proxy.downstream.service.ObservationPublisherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class LoggerObservationPublisherServiceImpl implements ObservationPublisherService {

	Logger logger = LoggerFactory.getLogger(LoggerObservationPublisherServiceImpl.class);

	@Override
	public void publish(Observation observation) {
		DownstreamRequest primaryResult = observation.getPrimaryRequest();
		DownstreamRequest secondaryResult = observation.getSecondaryRequest();
		DownstreamRequest candidateResult = observation.getCandidateRequest();
		logger.info("Primary Http Response: {}", primaryResult);
		logger.info("Secondary Http Response: {}", secondaryResult);
		logger.info("Candidate Http Response: {}", candidateResult);
	}

}
