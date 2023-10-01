package org.malred.annotations.cache;

public @interface RedisConfig {
    String host() default "localhost";

    int port() default 6379;
}
