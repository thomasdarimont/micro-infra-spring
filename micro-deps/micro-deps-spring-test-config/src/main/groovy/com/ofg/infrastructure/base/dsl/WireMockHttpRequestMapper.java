package com.ofg.infrastructure.base.dsl;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;

/**
 * A class that contains static helper methods that map HTTP methods with given path
 */
public class WireMockHttpRequestMapper {
    public static MappingBuilder wireMockGet(String path) {
        return WireMock.get(WireMock.urlEqualTo(path));
    }

    public static MappingBuilder wireMockPut(String path) {
        return WireMock.put(WireMock.urlEqualTo(path));
    }

    public static MappingBuilder wireMockPost(String path) {
        return WireMock.post(WireMock.urlEqualTo(path));
    }

    public static MappingBuilder wireMockDelete(String path) {
        return WireMock.delete(WireMock.urlEqualTo(path));
    }

    public static MappingBuilder wireMockOptions(String path) {
        return WireMock.options(WireMock.urlEqualTo(path));
    }

    public static MappingBuilder wireMockHead(String path) {
        return WireMock.head(WireMock.urlEqualTo(path));
    }

}
