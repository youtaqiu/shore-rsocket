package io.irain.shore.rsocket.common.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.messaging.handler.annotation.MessageMapping;

import java.lang.annotation.*;

/**
 * rsocket handler annotation.
 * @author youta
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MessageMapping
public @interface RSocketHandler {

    /**
     * The value of the annotation.
     * @return the value of the annotation.
     */
    @AliasFor(annotation = MessageMapping.class)
    String[] value() default {};
}
