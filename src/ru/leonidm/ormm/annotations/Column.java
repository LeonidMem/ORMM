package ru.leonidm.ormm.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    String name() default "";

    boolean unique() default false;
    boolean notNull() default false;

    int length() default -1;
    boolean index() default false;

    Class<?> databaseClass() default Void.class;
    String loadFunction() default "";
    String saveFunction() default "";

    // TODO: default value

}
