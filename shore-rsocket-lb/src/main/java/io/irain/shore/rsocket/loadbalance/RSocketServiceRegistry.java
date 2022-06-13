package io.irain.shore.rsocket.loadbalance;

import io.rsocket.loadbalance.LoadbalanceTarget;
import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * rsocket service registry
 * @author youta
 */
public interface RSocketServiceRegistry {

    /**
     * get servers
     * @param serviceName service name
     * @return load balance targets
     */
    Flux<List<LoadbalanceTarget>> getServers(String serviceName);

    /**
     * build rsocket server instances.
     * @param serviceName service name
     * @param builder rSocket requester builder
     * @return rsocket requester
     */
    RSocketRequester buildLoadBalanceRSocket(String serviceName, RSocketRequester.Builder builder);

    /**
     * Get snapshot of rsocket server instances.
     * @return rsocket server instances
     */
    Map<String, List<RSocketServerInstance>> getSnapshots();

    /**
     * Get last refresh timestamp.
     * @return last refresh timestamp
     */
    Date getLastRefreshTimestamp();
}
