package com.ofg.infrastructure.discovery.web;

import com.ofg.infrastructure.discovery.ServiceConfigurationResolver;
import com.ofg.infrastructure.stub.Stubs;
import com.ofg.stub.StubRunning;
import com.ofg.stub.server.AvailablePortScanner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.base.Function;

/**
 * Configuration that registers {@link HttpMockServer} as a Spring bean. Takes care
 * of graceful shutdown process.
 *
 * @see HttpMockServer
 */
@Configuration
public class MockServerConfiguration {

    @Bean(destroyMethod = "shutdownServer")
    public HttpMockServer httpMockServer(AvailablePortScanner availablePortScanner) {
        return availablePortScanner.tryToExecuteWithFreePort(new Function<Integer, HttpMockServer>() {
            @Override
            public HttpMockServer apply(Integer availablePort) {
                HttpMockServer httpMockServer = new HttpMockServer(availablePort);
                httpMockServer.start();
                return httpMockServer;
            }
        });
    }

    @Bean
    public AvailablePortScanner availablePortScanner() {
        return new AvailablePortScanner(8030, 10000);
    }

    @Bean(destroyMethod = "shutdown")
    public Stubs stubs(ServiceConfigurationResolver configurationResolver, StubRunning stubRunning) {
        return new Stubs(configurationResolver, stubRunning);
    }

}
