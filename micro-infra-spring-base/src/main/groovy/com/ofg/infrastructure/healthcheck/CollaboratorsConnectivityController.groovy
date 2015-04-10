package com.ofg.infrastructure.healthcheck

import com.google.common.base.Function
import com.google.common.base.Optional
import com.google.common.base.Predicate
import com.google.common.collect.Iterables
import com.google.common.collect.Maps
import com.ofg.infrastructure.discovery.ServiceAlias
import com.ofg.infrastructure.discovery.ServicePath
import com.ofg.infrastructure.discovery.ServiceResolver
import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import groovy.util.logging.Slf4j
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

/**
 * {@link RestController} providing connection state with services the microservice depends upon.
 */
@Slf4j
@RestController
@CompileStatic
@PackageScope
@RequestMapping(value = '/collaborators', method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
class CollaboratorsConnectivityController {

    private final ServiceResolver serviceResolver
    private final PingClient pingClient

    CollaboratorsConnectivityController(ServiceResolver serviceResolver, PingClient pingClient) {
        this.serviceResolver = serviceResolver
        this.pingClient = pingClient
    }

    /**
     * Returns information about connection status of microservice with other microservices.
     * For properly connected service <b>UP</b> state is provided and <b>DOWN</b> otherwise.
     *
     * @return connection status
     */
    @RequestMapping
    Map getCollaboratorsConnectivityInfo() {
        Map result = [:]
        Set<ServicePath> myCollaborators = serviceResolver.fetchMyDependencies()
        for (ServicePath servicePath : myCollaborators) {
            result.put(servicePath.path, statusOfAllCollaboratorInstances(servicePath))
        }
        return result
    }

    private Map statusOfAllCollaboratorInstances(ServicePath service) {
        Map result = [:]
        Set<URI> allUrisOfService = serviceResolver.fetchAllUris(service)
        for (URI instanceUrl: allUrisOfService) {
            boolean status = checkConnectionStatus(instanceUrl)
            result.put(instanceUrl, CollaboratorStatus.of(status))
        }
        return result
    }

    @RequestMapping('/all')
    Map getAllCollaboratorsConnectivityInfo() {
        Map result = [:]
        final Set<ServicePath> allServices = serviceResolver.fetchAllDependencies()
        for (ServicePath service : allServices) {
            result.put(service.path, collaboratorsStatusOfAllInstances(service))
        }
        return result
    }

    private Map collaboratorsStatusOfAllInstances(ServicePath service) {
        Map result = [:]
        final Set<URI> collaboratorInstances = serviceResolver.fetchAllUris(service)
        for (URI uri : collaboratorInstances) {
            result.put(uri, checkCollaborators(uri))
        }
        return result
    }

    private Map checkCollaborators(URI url) {
        Optional<Map> collaborators = establishCollaboratorsStatus(url)
        Map result = [:]
        result.put('status', CollaboratorStatus.of(collaborators.isPresent()))
        result.put('collaborators', collaborators.or([:]))
        return result
    }

    private Optional<Map> establishCollaboratorsStatus(URI url) {
        Optional<Map> collaborators = tryCallingCollaborators(url)
        return fallbackWithPingIfCollaboratorsFailed(collaborators, url)
    }

    Optional<Map> fallbackWithPingIfCollaboratorsFailed(Optional<Map> maybeCollaborators, URI url) {
        if (maybeCollaborators.isPresent()) {
            return maybeCollaborators
        }
        return checkConnectionStatus(url) ?
                Optional.of([:]) :
                Optional.absent()
    }

    private Optional<Map> tryCallingCollaborators(URI url) {
        Optional<Map> collaborators = pingClient
                .checkCollaborators(url)
                .transform(new Function<Map, Map>() {
            @Override
            Map apply(Map input) {
                return tryAdjustLegacyCollaboratorsResponse(input)
            }
        })
        return collaborators
    }

    private Map tryAdjustLegacyCollaboratorsResponse(Map collaboratorsResponse) {
        if (isLegacyResponse(collaboratorsResponse)) {
            return adjustLegacyCollaboratorsResponse(collaboratorsResponse)
        } else {
            return collaboratorsResponse;
        }
    }

    private Map adjustLegacyCollaboratorsResponse(Map collaboratorsResponse) {
        Map result = [:]
        for (Map.Entry<Object, Object> entry : collaboratorsResponse.entrySet()) {
            tryResolveAlias(entry.getKey() as String).transform (new Function() {
                @Override
                Object apply(Object path) {
                    result.putAll(suspectedStatusOfAllInstances(path as ServicePath, entry.getValue() as String))
                    return result
                }
            })
        }
        return result
    }

    private Map suspectedStatusOfAllInstances(ServicePath service, String statusStr) {
        final Set<URI> allInstances = serviceResolver.fetchAllUris(service)
        CollaboratorStatus status = CollaboratorStatus.of(statusStr == 'CONNECTED')
        Map suspectedStatuses = [:]
        for (URI uri : allInstances) {
            suspectedStatuses.put(uri, status)
        }
        Map result = [:]
        result.put(service.path, suspectedStatuses)
        return result
    }

    private Optional<ServicePath> tryResolveAlias(String alias) {
        try {
            ServiceAlias serviceAlias = new ServiceAlias(alias as String)
            return Optional.of(serviceResolver.resolveAlias(serviceAlias))
        } catch (NoSuchElementException e) {
            log.warn("Unable to resolve alias $alias", e)
            return Optional.absent()
        }
    }

    private boolean isLegacyResponse(Map collaboratorsResponse) {
        boolean nonMapInstances = Iterables.any(collaboratorsResponse.values(), new Predicate(){
            @Override
            boolean apply(Object input) {
                return !(input instanceof Map)
            }
        })
        return !collaboratorsResponse.isEmpty() && nonMapInstances
    }

    private boolean checkConnectionStatus(URI url) {
        final Optional<String> pingResult = pingClient.ping(url)
        return pingResult == Optional.of('OK') ||
                pingResult == Optional.of('')
    }

}
