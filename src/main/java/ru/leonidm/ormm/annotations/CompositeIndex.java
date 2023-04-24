package ru.leonidm.ormm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(CompositeIndex.Repeated.class)
public @interface CompositeIndex {

    /**
     * Name of the columns
     */
    String[] value();

    boolean unique() default false;

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Repeated {

        CompositeIndex[] value();

    }
}
