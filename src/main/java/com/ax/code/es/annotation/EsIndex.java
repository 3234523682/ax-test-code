package com.ax.code.es.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author lj
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EsIndex {
    String indexName();

    String type();

    int shards() default 1;

    int replicas() default 5;

    String refreshInterval() default "1";
}
