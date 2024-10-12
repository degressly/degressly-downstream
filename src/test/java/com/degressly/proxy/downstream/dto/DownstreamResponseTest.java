package com.degressly.proxy.downstream.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(MockitoJUnitRunner.class)
public class DownstreamResponseTest {

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void testSerialization() throws JsonProcessingException {
		System.out.println(objectMapper.readValue("{\n" + "    \"headers\": {\n" + "        \"Date\": [\n"
				+ "            \"Sat, 12 Oct 2024 13:39:32 GMT\"\n" + "        ],\n" + "        \"Server\": [\n"
				+ "            \"nginx/1.25.3\"\n" + "        ],\n" + "        \"set-cookie\": [\n"
				+ "            \"sails.sid=s%3A1y3-fUAfpliBTxMMioAo_G_FqlZ-EVhx.C7pYOEw7jnK7k%2Fkv0nUKiWdJZpXwZ7OtkgbzoI8MHaE; Path=/; HttpOnly\"\n"
				+ "        ]\n" + "    },\n" + "    \"body\": \"\",\n" + "    \"statusCode\": 404\n" + "}",
				DownstreamResponse.class));
	}

}