package io.irain.shore.rsocket.loadbalance;

import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.cbor.Jackson2CborDecoder;
import org.springframework.http.codec.cbor.Jackson2CborEncoder;
import org.springframework.messaging.rsocket.RSocketStrategies;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * rsocket load balance configuration
 * @author youta
 */
@Configuration
@EnableScheduling
public class RSocketLoadBalanceConfiguration {

    /**
     * rsocket service discovery registry bean
     * @param discoveryClient discovery client
     * @return rsocket service discovery registry
     */
    @Bean
    public RSocketServiceDiscoveryRegistry rsocketServiceDiscoveryRegistry(ReactiveDiscoveryClient discoveryClient) {
        return new RSocketServiceDiscoveryRegistry(discoveryClient);
    }

    /**
     * rsocket load balance endpoint bean
     * @param rsocketServiceRegistry rsocket service registry
     * @return rsocket load balance endpoint
     */
    @Bean
    public RSocketLoadBalanceEndpoint rsocketLoadBalanceEndpoint(RSocketServiceRegistry rsocketServiceRegistry) {
        return new RSocketLoadBalanceEndpoint(rsocketServiceRegistry);
    }

    /**
     * rSocketStrategies bean
     * @return {@link RSocketStrategies}
     */
    @Bean
    public RSocketStrategies rSocketStrategies() {
        return RSocketStrategies.builder()
                .encoders(encoders -> encoders.add(new Jackson2CborEncoder()))
                .decoders(decoders -> decoders.add(new Jackson2CborDecoder()))
                .build();
    }

}
