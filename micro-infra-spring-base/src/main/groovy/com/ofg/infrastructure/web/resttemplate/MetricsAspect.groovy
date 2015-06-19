package com.ofg.infrastructure.web.resttemplate

import com.codahale.metrics.MetricRegistry
import com.ofg.infrastructure.web.resttemplate.fluent.UrlMetricNameProvider
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.http.RequestEntity
import org.springframework.web.client.RestOperations

@Aspect
@CompileStatic
@Slf4j
class MetricsAspect {

    private static final String PREFIX = RestOperations.class.simpleName
    private final MetricRegistry metricRegistry
    private final UrlMetricNameProvider urlMetricNameProvider

    MetricsAspect(MetricRegistry metricRegistry, UrlMetricNameProvider urlMetricNameProvider) {
        this.metricRegistry = metricRegistry
        this.urlMetricNameProvider = urlMetricNameProvider
    }

    @Around('execution(public * org.springframework.web.client.RestOperations.*(..))')
    Object measureRestCallDuration(ProceedingJoinPoint pjp) throws Throwable {
        URI uri = getUriForMetricNaming(pjp)
        final String name = urlMetricNameProvider.metricName(uri)
        return metricRegistry.timer("${PREFIX}.${name}").time {
            final long startTime = System.currentTimeMillis()
            Object result = pjp.proceed()
            final long done = System.currentTimeMillis() - startTime
            log.debug("Call to '$uri' [$name] took ${done}ms")
            return result
        }
    }

    private URI getUriForMetricNaming(ProceedingJoinPoint pjp) {
        def firstArgument = pjp.args[0]
        URI uri
        if (firstArgument instanceof URI) {
            uri = (URI) firstArgument
        } else if (firstArgument instanceof RequestEntity) {
            def requestEntity = (RequestEntity) firstArgument
            uri = requestEntity.url
        } else if (firstArgument instanceof String) {
            String urlTemplateString = (String) firstArgument
            String approximateUriString = urlTemplateString.replaceAll(/[{}]/, '')
            uri = new URI(approximateUriString)
        } else {
            throw new IllegalArgumentException(
                    'Could not get uri from a RestOperations.* method call. Did they change the interface?'
            )
        }
        return uri
    }
}
