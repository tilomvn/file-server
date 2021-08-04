package com.treasuredata.fileserver.order;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface TestOrder {
    int value() default 0;
}
