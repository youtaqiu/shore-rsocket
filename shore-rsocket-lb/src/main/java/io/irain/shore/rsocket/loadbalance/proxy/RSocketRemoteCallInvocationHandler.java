package io.irain.shore.rsocket.loadbalance.proxy;

import org.springframework.messaging.rsocket.RSocketRequester;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * rSocket remote call invocation handler.
 * @author youta
 */
public class RSocketRemoteCallInvocationHandler implements InvocationHandler {
    private final RSocketRequester rsocketRequester;
    private final Class<?> serviceInterface;
    private final String serviceName;
    private static final  Map<Method, Class<?>> methodReturnTypeMap = new HashMap<>();

    /**
     * Constructor.
     * @param rsocketRequester rSocket requester
     * @param serviceName service name
     * @param serviceInterface service interface
     */
    public RSocketRemoteCallInvocationHandler(RSocketRequester rsocketRequester, String serviceName, Class<?> serviceInterface) {
        this.rsocketRequester = rsocketRequester;
        this.serviceName = serviceName;
        this.serviceInterface = serviceInterface;
    }

    /**
     * Invoke.
     * @param proxy the proxy instance that the method was invoked on
     *
     * @param method the {@code Method} instance corresponding to
     * the interface method invoked on the proxy instance.  The declaring
     * class of the {@code Method} object will be the interface that
     * the method was declared in, which may be a superinterface of the
     * proxy interface that the proxy class inherits the method through.
     *
     * @param args an array of objects containing the values of the
     * arguments passed in the method invocation on the proxy instance,
     * or {@code null} if interface method takes no arguments.
     * Arguments of primitive types are wrapped in instances of the
     * appropriate primitive wrapper class, such as
     * {@code java.lang.Integer} or {@code java.lang.Boolean}.
     *
     * @return the value to return from the method invocation on the
     * @throws Throwable the exception to throw from the method
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.isDefault()) {
            return DefaultMethodHandler.getMethodHandle(method, serviceInterface).bindTo(proxy).invokeWithArguments(args);
        }
        String methodName = method.getName();
        Object arg = null;
        if (args != null && args.length > 0) {
            arg = args[0];
        }
        Class<?> returnType = methodReturnTypeMap.get(method);
        if (returnType == null) {
            returnType = parseInferredClass(method.getGenericReturnType());
            methodReturnTypeMap.put(method, returnType);
        }
        RSocketRequester.RequestSpec requestSpec = rsocketRequester.route(serviceName + "." + methodName);
        RSocketRequester.RetrieveSpec retrieveSpec;
        if (arg != null) {
            retrieveSpec = requestSpec.data(arg);
        } else {
            retrieveSpec = requestSpec;
        }
        // Flux return type: request/stream or channel
        if (method.getReturnType().isAssignableFrom(Flux.class)) {
            return retrieveSpec.retrieveFlux(returnType);
        } else { //Mono return type
            // Void return type: fireAndForget
            if (returnType.equals(Void.class)) {
                return retrieveSpec.send();
            } else { // request/response
                return requestSpec.retrieveMono(returnType);
            }
        }
    }

    /**
     * Parse inferred class.
     * @param genericType generic type
     * @return inferred class
     */
    public static Class<?> parseInferredClass(Type genericType) {
        Class<?> inferredClass = null;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType type = (ParameterizedType) genericType;
            Type[] typeArguments = type.getActualTypeArguments();
            if (typeArguments.length > 0) {
                final Type typeArgument = typeArguments[0];
                if (typeArgument instanceof ParameterizedType) {
                    inferredClass = (Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0];
                } else if (typeArgument instanceof Class) {
                    inferredClass = (Class<?>) typeArgument;
                } else {
                    String typeName = typeArgument.getTypeName();
                    if (typeName.contains(" ")) {
                        typeName = typeName.substring(typeName.lastIndexOf(" ") + 1);
                    }
                    if (typeName.contains("<")) {
                        typeName = typeName.substring(0, typeName.indexOf("<"));
                    }
                    try {
                        inferredClass = Class.forName(typeName);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        if (inferredClass == null && genericType instanceof Class) {
            inferredClass = (Class<?>) genericType;
        }
        return inferredClass;
    }
}
