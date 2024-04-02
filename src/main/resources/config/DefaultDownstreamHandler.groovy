package config

import com.degressly.proxy.downstream.dto.RequestContext
import groovy.json.JsonSlurper
import org.springframework.util.MultiValueMap

class DownstreamHandler implements com.degressly.proxy.downstream.handler.DownstreamHandler {

    Set<String> idempotentURIs = Set.of("/sample-idempotent", );

    @Override
    Optional<Boolean> isIdempotent(RequestContext requestContext) {
        if (idempotentURIs.contains(requestContext.getRequest().getRequestURI())) {
            return Optional.of(Boolean.TRUE)
        } else {
            return Optional.of(Boolean.FALSE)
        }

    }

    @Override
    Optional<String> getTraceId(RequestContext requestContext) {

        JsonSlurper jsonSlurper = new JsonSlurper()
        def bodyJson

        try {
            bodyJson = jsonSlurper.parseText(requestContext.getBody())
        } catch(Exception ignored) {
            // Do nothing
            bodyJson = null
        }

        def optional

        optional = getField(requestContext.getHeaders(), requestContext.getParams(), bodyJson,"trace-id")
        if (optional.isPresent())
            return Optional.of(optional.get())

        optional = getField(requestContext.getHeaders(), requestContext.getParams(), bodyJson,"seqNo")
        if (optional.isPresent())
            return Optional.of(optional.get())

        optional = getField(requestContext.getHeaders(), requestContext.getParams(), bodyJson,"seq-no")
        if (optional.isPresent())
            return Optional.of((optional.get()))

        optional = getField(requestContext.getHeaders(), requestContext.getParams(), bodyJson,"txn-id")
        if (optional.isPresent())
            return Optional.of((optional.get()))

        optional = getField(requestContext.getHeaders(), requestContext.getParams(), bodyJson,"txnId")
        if (optional.isPresent())
            return Optional.of(optional.get())

        return Optional.empty()
    }

    @Override
    Optional<String> getIdempotencyKey(RequestContext requestContext) {
        if (requestContext.getTraceId()) {
            return Optional.of(requestContext.getRequest().getRequestURL().append("_").append(requestContext.getTraceId()).toString());
        }

        return Optional.empty();
    }

    private static Optional<String> getField(MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Object bodyJson, String field) {
        if (headers.containsKey(field))
            return Optional.of(headers.getFirst(field))
        if (params.containsKey(field))
            return Optional.of(params.getFirst(field))
        if (bodyJson!=null && bodyJson[field] != null && bodyJson[field] instanceof String)
            return Optional.of((String) bodyJson[field])

        return Optional.empty()
    }
}