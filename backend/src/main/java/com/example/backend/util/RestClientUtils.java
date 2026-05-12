package com.example.backend.util;

import org.apache.hc.core5.net.URIBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Simple helper for outbound HTTP calls using RestClient.
 */
public final class RestClientUtils {

    private static final RestClient REST_CLIENT = RestClient.create();

    private RestClientUtils() {
    }

    public static <T> ResponseEntity<T> exchange(
            String url,
            HttpMethod method,
            Map<String, String> headers,
            Object requestBody,
            Class<T> responseType
    ) {
        URI uri = URI.create(url);
        return prepareRequest(uri, method, headers, requestBody)
                .retrieve()
                .toEntity(responseType);
    }

    public static <T> ResponseEntity<T> exchange(
            String url,
            HttpMethod method,
            Map<String, String> headers,
            Object requestBody,
            ParameterizedTypeReference<T> responseType
    ) {
        URI uri = URI.create(url);
        return prepareRequest(uri, method, headers, requestBody)
                .retrieve()
                .toEntity(responseType);
    }

    public static String buildUrl(
            String baseUrl,
            String contextPath,
            String relativePath,
            Map<String, ?> queryParams
    ) {
        try {
            URIBuilder builder = baseUrl == null || baseUrl.isBlank()
                    ? new URIBuilder()
                    : new URIBuilder(baseUrl);

            if (contextPath != null && !contextPath.isBlank()) {
                builder.appendPath(contextPath);
            }

            if (relativePath != null && !relativePath.isBlank()) {
                builder.appendPath(relativePath);
            }

            if (queryParams != null && !queryParams.isEmpty()) {
                queryParams.forEach((key, value) -> {
                    if (value != null) {
                        builder.addParameter(key, String.valueOf(value));
                    }
                });
            }

            return builder.build().toString();
        } catch (URISyntaxException exception) {
            throw new IllegalArgumentException("Invalid URL parts", exception);
        }
    }

    private static RestClient.RequestHeadersSpec<?> prepareRequest(
            URI uri,
            HttpMethod method,
            Map<String, String> headers,
            Object requestBody
    ) {
        RestClient.RequestBodySpec requestSpec = REST_CLIENT.method(method)
                .uri(uri)
                .headers(httpHeaders -> {
                    if (headers != null && !headers.isEmpty()) {
                        headers.forEach(httpHeaders::add);
                    }
                });

        if (requestBody == null) {
            return requestSpec;
        }

        return requestSpec.body(requestBody);
    }

    /*
     * Example usage:
     *
     * ResponseEntity<String> loginResponse = RestClientUtils.exchange(
     *         "http://localhost:8080/api/auth/login",
     *         HttpMethod.POST,
     *         Map.of("Authorization", "Bearer token"),
     *         loginRequest
     *         String.class
     * );
     *
     * ResponseEntity<MyResponseDto> processResponse = RestClientUtils.exchange(
     *         "http://localhost:8080/api/processes/{id}",
     *         HttpMethod.GET,
     *         Map.of("id", 101),
     *         null,
     *         MyResponseDto.class
     * );
     *
     * ResponseEntity<List<MyResponseDto>> listResponse = RestClientUtils.exchange(
     *         "http://localhost:8080/api/processes",
     *         HttpMethod.GET,
     *         Map.of("status", "ACTIVE"),
     *         null,
     *         new ParameterizedTypeReference<List<MyResponseDto>>() {}
     * );
     *
     * String url = RestClientUtils.buildUrl(
     *         "http://localhost:8080",
     *         "/api",
     *         "/processes",
     *         Map.of("status", "ACTIVE")
     * );
     * // Result: http://localhost:8080/api/processes?status=ACTIVE
     *
     * String urlWithoutSlashes = RestClientUtils.buildUrl(
     *         "http://localhost:8080",
     *         "api",
     *         "processes",
     *         Map.of("status", "ACTIVE")
     * );
     * // Result is the same: http://localhost:8080/api/processes?status=ACTIVE
     *
     * String urlWithTrailingSlash = RestClientUtils.buildUrl(
     *         "http://localhost:8080/",
     *         "/api/",
     *         "/processes/",
     *         null
     * );
     * // Result keeps the trailing slash: http://localhost:8080/api/processes/
     */
}