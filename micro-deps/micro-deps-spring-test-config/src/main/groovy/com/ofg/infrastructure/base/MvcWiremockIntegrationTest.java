package com.ofg.infrastructure.base;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.ofg.config.BasicProfiles;
import com.ofg.infrastructure.discovery.web.HttpMockServer;
import com.ofg.infrastructure.discovery.web.MockServerConfiguration;
import com.ofg.infrastructure.stub.Stub;
import com.ofg.infrastructure.stub.Stubs;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Base specification for tests that use Wiremock as HTTP server stub.
 * By extending this specification you gain a bean with {@link HttpMockServer} and a {@link WireMock}
 * instance that you can stub by using {@link MvcWiremockIntegrationSpec#stubInteraction(MappingBuilder, ResponseDefinitionBuilder)}
 *
 * @see MockServerConfiguration
 * @see WireMock
 * @see HttpMockServer
 * @see MvcIntegrationSpec
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {MockServerConfiguration.class})
@ActiveProfiles(BasicProfiles.TEST)
public abstract class MvcWiremockIntegrationTest extends MvcIntegrationTest {
    protected WireMock wireMock;
    @Autowired protected HttpMockServer httpMockServer;
    @Autowired protected Stubs stubs;

    @Before
    public void setup() {
        super.setup();
        wireMock = new WireMock("localhost", httpMockServer.port());
        wireMock.resetToDefaultMappings();
    }

    protected void stubInteraction(MappingBuilder mapping, ResponseDefinitionBuilder response) {
        wireMock.register(mapping.willReturn(response));
    }

    protected Stub stubOf(String collaboratorName) {
        return stubs.of(collaboratorName);
    }

    protected void cleanup() {
        stubs.resetAll();
    }
}
