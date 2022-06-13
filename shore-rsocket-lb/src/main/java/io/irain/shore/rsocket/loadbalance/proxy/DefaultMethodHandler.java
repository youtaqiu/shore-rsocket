package io.irain.shore.rsocket.loadbalance.proxy;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Default Method Handler
 *
 * <code>
 * if (method.isDefault()) {
 * return DefaultMethodHandler.getMethodHandle(method, serviceInterface).bindTo(proxy).invokeWithArguments(args);
 * }
 * </code>
 *
 * @author linux_china
 */
public class DefaultMethodHandler {
    /**
     * default method handles
     */
    private static final Map<Method, MethodHandle> methodHandles = new HashMap<>();

    /**
     * get method handle
     *
     * @param method method
     * @param serviceInterface service interface
     * @throws Exception exception
     * @return method handle
     */
    public static MethodHandle getMethodHandle(Method method, Class<?> serviceInterface) throws Exception {
        MethodHandle methodHandle = methodHandles.get(method);
        if (methodHandle == null) {
            String version = System.getProperty("java.version");
            if (version.startsWith("1.8.")) {
                Constructor<MethodHandles.Lookup> lookupConstructor = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, Integer.TYPE);

                if (!lookupConstructor.canAccess(null)) {
                    lookupConstructor.setAccessible(true);
                }
                methodHandle = lookupConstructor.newInstance(method.getDeclaringClass(), MethodHandles.Lookup.PRIVATE)
                        .unreflectSpecial(method, method.getDeclaringClass());
            } else {
                methodHandle = MethodHandles.lookup().findSpecial(
                        method.getDeclaringClass(),
                        method.getName(),
                        MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
                        serviceInterface);
            }
            methodHandles.put(method, methodHandle);
        }
        return methodHandle;
    }
}
