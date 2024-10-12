package com.degressly.proxy.downstream.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class MultiValueMapDeserializer extends JsonDeserializer<MultiValueMap<String, String>> {

	@Override
	public MultiValueMap<String, String> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		// Correctly construct the types for the Map<String, List<String>>
		TypeFactory typeFactory = ctxt.getTypeFactory();
		Map<String, List<String>> map = ctxt.readValue(p,
				typeFactory.constructMapType(Map.class, typeFactory.constructType(String.class),
						typeFactory.constructCollectionType(List.class, String.class)));
		MultiValueMap<String, String> multiValueMap = new LinkedMultiValueMap<>();
		multiValueMap.putAll(map);
		return multiValueMap;
	}

}