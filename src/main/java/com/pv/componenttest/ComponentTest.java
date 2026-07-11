package com.pv.componenttest;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;

@Target(TYPE)
@Retention(RUNTIME)

@SpringBootTest
public @interface ComponentTest {}
