package io.irain.shore.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import io.rsocket.loadbalance.RoundRobinLoadbalanceStrategy;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.scheduling.annotation.Scheduled;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * rsocket load balance registry
 * @author youta
 */
public class RSocketServiceDiscoveryRegistry implements RSocketServiceRegistry {
    /**
     * appName and server instance list mapping
     */
    private final Map<String, Sinks.Many<List<RSocketServerInstance>>> service2Servers = new ConcurrentHashMap<>();

    private final Map<String, List<RSocketServerInstance>> snapshots = new HashMap<>();
    private final ReactiveDiscoveryClient discoveryClient;
    private Date lastRefreshTimeStamp = new Date();
    private boolean refreshing = false;

    /**
     * Constructor.
     * @param discoveryClient discovery client
     */
    public RSocketServiceDiscoveryRegistry(ReactiveDiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    /**
     * Get snapshot of rsocket server instances.
     * @return rsocket server instances
     */
    @Override
    public Map<String, List<RSocketServerInstance>> getSnapshots() {
        return this.snapshots;
    }

    /**
     * Get last refresh timestamp.
     * @return last refresh timestamp
     */
    @Override
    public Date getLastRefreshTimestamp() {
        return this.lastRefreshTimeStamp;
    }

    /**
     * Refresh rsocket server instances.
     */
    @Scheduled(initialDelay = 5000, fixedRate = 15000)
    public void refreshServers() {
        if (!refreshing) {
            refreshing = true;
            lastRefreshTimeStamp = new Date();
            try {
                if (!snapshots.isEmpty()) {
                    for (String serviceName : service2Servers.keySet()) {
                        discoveryClient.getInstances(serviceName)
                                .map(this::convertToRSocketServerInstance)
                                .collectList().subscribe(newServiceInstances -> {
                            List<RSocketServerInstance> currentServerInstances = snapshots.get(serviceName);
                            //not same
                            if (!(currentServerInstances.size() == newServiceInstances.size() && new HashSet<>(currentServerInstances).containsAll(newServiceInstances))) {
                                setServers(serviceName, newServiceInstances);
                            }
                        });
                    }
                }
            } finally {
                refreshing = false;
            }
        }
    }

    /**
     * Set rsocket server instances.
     * @param serviceName service name
     * @param servers rsocket server instances
     */
    public void setServers(String serviceName, List<RSocketServerInstance> servers) {
        String appName = convertToAppName(serviceName);
        if (service2Servers.containsKey(appName)) {
            this.service2Servers.get(appName).tryEmitNext(servers);
            this.snapshots.put(appName, servers);
        }
    }

    /**
     * build load balance rsocket.
     * @param serviceName service name
     * @param builder builder
     * @return rsocket requester
     */
    @Override
    public RSocketRequester buildLoadBalanceRSocket(String serviceName, RSocketRequester.Builder builder) {
        return builder.transports(this.getServers(serviceName), new RoundRobinLoadbalanceStrategy());
    }

    /**
     * Get rsocket server instances.
     * @param serviceName service name
     * @return load balance target
     */
    public Flux<List<LoadbalanceTarget>> getServers(String serviceName) {
        final String appName = convertToAppName(serviceName);
        if (!service2Servers.containsKey(appName)) {
            service2Servers.put(appName, Sinks.many().replay().latest());
            return Flux.from(discoveryClient.getInstances(appName)
                    .map(this::convertToRSocketServerInstance)
                    .collectList()
                    .doOnNext(rSocketServerInstances -> {
                        snapshots.put(appName, rSocketServerInstances);
                        service2Servers.get(appName).tryEmitNext(rSocketServerInstances);
                    }))
                    .thenMany(service2Servers.get(appName).asFlux().map(this::toLoadBalanceTarget));
        }
        return service2Servers.get(appName)
                .asFlux()
                .map(this::toLoadBalanceTarget);
    }

    /**
     * Convert to load balance target.
     * @param rSocketServers rsocket server instances
     * @return load balance target
     */
    private List<LoadbalanceTarget> toLoadBalanceTarget(List<RSocketServerInstance> rSocketServers) {
        return rSocketServers.stream()
                .map(server -> LoadbalanceTarget.from(server.getHost() + server.getPort(), server.constructClientTransport()))
                .collect(Collectors.toList());
    }

    /**
     * Convert to rsocket server instance.
     * @param serviceName service name
     * @return rsocket server instance
     */
    private String convertToAppName(String serviceName) {
        String appName = serviceName.replaceAll("\\.", "-");
        // service with assigned name
        if (serviceName.contains(":")) {
            return appName.substring(0, appName.indexOf(":"));
        }
        if (appName.contains("-")) {
            String temp = appName.substring(appName.lastIndexOf("-") + 1);
            // if first character is uppercase, and it means service name
            if (Character.isUpperCase(temp.toCharArray()[0])) {
                appName = appName.substring(0, appName.lastIndexOf("-"));
            }
        }
        return appName;
    }

    /**
     * Convert to rsocket server instance.
     * @param serviceInstance service instance
     * @return rsocket server instance
     */
    private RSocketServerInstance convertToRSocketServerInstance(ServiceInstance serviceInstance) {
        RSocketServerInstance serverInstance = new RSocketServerInstance();
        serverInstance.setHost(serviceInstance.getHost());
        serverInstance.setSchema(serviceInstance.getMetadata().getOrDefault("rsocketSchema", "tcp"));
        if (serverInstance.isWebSocket()) {
            serverInstance.setPort(serviceInstance.getPort());
            serverInstance.setPath(serviceInstance.getMetadata().getOrDefault("rsocketPath", "/rsocket"));
        } else {
            serverInstance.setPort(Integer.parseInt(serviceInstance.getMetadata().getOrDefault("rsocketPort", "42252")));
        }
        return serverInstance;
    }

}
