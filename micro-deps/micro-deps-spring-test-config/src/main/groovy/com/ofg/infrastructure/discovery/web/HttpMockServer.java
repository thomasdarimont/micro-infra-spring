package com.ofg.infrastructure.discovery.web;

import com.github.tomakehurst.wiremock.WireMockServer;

/**
 * Custom implementation of {@link WireMockServer} that by default registers itself at port
 * {@link HttpMockServer#DEFAULT_PORT}.
 *
 * @see WireMockServer
 */
public class HttpMockServer extends WireMockServer {
    public static final int DEFAULT_PORT = 8030;

    public HttpMockServer(int port) {
        super(port);
    }

    public HttpMockServer() {
        super(DEFAULT_PORT);
    }

    public void shutdownServer() {
        if (isRunning()) {
            stop();
        }
        shutdown();
    }
}
