package com.ofg.infrastructure.stub;

import com.github.tomakehurst.wiremock.client.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.ListStubMappingsResult;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;

import java.util.List;

public class Stub {
    public Stub(String host, int port) {
        this.delegate = new WireMock(host, port);
    }

    /**
     * Verifies that a single call to the mock with given expected request criteria took place.
     *
     * @param requestPatternBuilder expected request criteria
     */
    public void verifyThat(RequestPatternBuilder requestPatternBuilder) {
        delegate.verifyThat(requestPatternBuilder);
    }

    /**
     * Verifies that multiple calls ({@code count} times) to the mock with given expected request criteria took place.
     *
     * @param count                 expectation on how many times the interaction with mock took place
     * @param requestPatternBuilder expected request criteria
     */
    public void verifyThat(int count, RequestPatternBuilder requestPatternBuilder) {
        delegate.verifyThat(count, requestPatternBuilder);
    }

    /**
     * Resets all interactions and mappings for this stub to the starting point,
     * i.e. with no interactions registered and mappings loaded from stub mappings definitions.
     */
    public void resetToDefaults() {
        ListStubMappingsResult stubMappings = delegate.allStubMappings();
        delegate.resetToDefaultMappings();
        reRegister(delegate, stubMappings);
    }

    private List reRegister(WireMock mock, ListStubMappingsResult stubMappings) {
        List<StubMapping> mappings =  stubMappings.getMappings();
        for (StubMapping stubMapping : mappings) {
            mock.register(stubMapping);
        }
        return mappings;
    }

    public void shutdown() {
        delegate.shutdown();
    }

    private WireMock delegate;
}
