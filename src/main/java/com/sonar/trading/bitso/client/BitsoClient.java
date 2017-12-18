package com.sonar.trading.bitso.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sonar.trading.utils.StreamUtils;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class BitsoClient {

    private static final long MAX_BYTES_TO_READ_IN_ERROR_RESPONSE = 20480;

    private final BitsoClientConfig config;
    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper = createObjectMapper();

    public BitsoResponse<List<Trade>> getTrades(String book) throws IOException, URISyntaxException {
        return getTrades(book, null, Sort.desc, null);
    }

    public BitsoResponse<List<Trade>> getTrades(String book, Long marker, Sort sort, Integer limit) throws IOException, URISyntaxException {
        Objects.requireNonNull(book);
        URIBuilder builder = new URIBuilder(config.getEndpoint() + "/v3/trades/");
        builder.addParameter("book", book);
        if (marker != null) {
            builder.addParameter("marker", String.valueOf(marker));
        }
        if (sort != null) {
            builder.addParameter("sort", String.valueOf(sort));
        }
        if (limit != null) {
            builder.addParameter("limit", String.valueOf(limit));
        }
        HttpGet request = new HttpGet(builder.build());
        try (CloseableHttpResponse response = httpClient.execute(request)) {
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                String body = readResponseBody(response);
                throw new IOException(String.format("Could not get trades from Bitso as http status was %s and body was %s", statusCode, body));
            }
            return objectMapper.readValue(response.getEntity().getContent(), new TypeReference<BitsoResponse<List<Trade>>>() {});
        }
    }

    private String readResponseBody(HttpResponse response) throws IOException {
        HttpEntity responseEntity = response.getEntity();
        String body = StreamUtils.readFully(responseEntity.getContent());
        return body;
    }

    private ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}
