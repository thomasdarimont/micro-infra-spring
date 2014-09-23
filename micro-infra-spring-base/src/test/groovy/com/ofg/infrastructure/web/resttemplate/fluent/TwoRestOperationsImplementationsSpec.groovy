package com.ofg.infrastructure.web.resttemplate.fluent

import com.ofg.infrastructure.base.BaseConfiguration
import com.ofg.infrastructure.base.MvcIntegrationSpec
import com.ofg.infrastructure.discovery.ServiceResolver
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.SpringApplicationContextLoader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.test.context.ContextConfiguration
import org.springframework.web.client.RestOperations

@ContextConfiguration(classes = [Config, BaseConfiguration, ServiceRestClientConfiguration],
        loader = SpringApplicationContextLoader)
class TwoRestOperationsImplementationsSpec extends MvcIntegrationSpec {

    @Autowired
    ComponentWithTwoRestOperationsImplementations componentWithTwoRestOperationsImplementations

    def "should allow to create additional, custom RestOperations implementation when there is already one registered in Spring context"() {
        expect:
            componentWithTwoRestOperationsImplementations.hasDependenciesInjectedCorrectly()
    }

    @Configuration
    static class Config {

        @Bean
        RestOperations customRestOperationsImplementation() {
            return new TestRestTemplate()
        }

        @Bean
        ComponentWithTwoRestOperationsImplementations componentWithTwoRestOperationsImplementations(RestOperations restOperations,
                                                                                                    ServiceRestClient serviceRestClient) {
            return new ComponentWithTwoRestOperationsImplementations(serviceRestClient, restOperations)
        }

        @Bean
        ServiceResolver stubForServiceResolver() {
            [:] as ServiceResolver
        }
    }

}
