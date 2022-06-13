package io.irain.shore.rsocket.loadbalance.proxy;

import org.springframework.messaging.rsocket.RSocketRequester;

import java.lang.reflect.Proxy;

/**
 * rsocket remote service builder.
 * @author youta
 * @param <T> the type of the target
 */
public class RSocketRemoteServiceBuilder<T> {
    private String serviceName;
    private Class<?> serviceInterface;
    private RSocketRequester rsocketRequester;

    /**
     * Constructor.
     * @param serviceInterface service interface
     * @return rsocket remote service builder
     */
    public RSocketRemoteServiceBuilder<T> serviceInterface(Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
        return this;
    }

    /**
     * Constructor.
     * @param serviceName service name
     * @return rsocket remote service builder
     */
    public RSocketRemoteServiceBuilder<T> serviceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    /**
     * Constructor.
     * @param rsocketRequester rSocket requester
     * @return rsocket remote service builder
     */
    public RSocketRemoteServiceBuilder<T> rsocketRequester(RSocketRequester rsocketRequester) {
        this.rsocketRequester = rsocketRequester;
        return this;
    }

    /**
     * Build.
     * @return rsocket remote call invocation handler
     */
    @SuppressWarnings("unchecked")
    public T build() {
        RSocketRemoteCallInvocationHandler handler = new RSocketRemoteCallInvocationHandler(rsocketRequester, serviceName, serviceInterface);
        return (T) Proxy.newProxyInstance(
                serviceInterface.getClassLoader(),
                new Class[]{serviceInterface},
                handler);
    }

}
