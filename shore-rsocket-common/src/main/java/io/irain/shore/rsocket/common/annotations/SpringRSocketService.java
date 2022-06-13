package io.irain.shore.rsocket.common.annotations;

import org.springframework.core.annotation.AliasFor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.lang.annotation.*;

/**
 * spring rsocket service annotation.
 * @author youta
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Controller
@MessageMapping
public @interface SpringRSocketService {

    /**
     * The value of the annotation.
     * @return the value of the annotation.
     */
    @AliasFor(annotation = MessageMapping.class)
    String[] value() default {};
}
