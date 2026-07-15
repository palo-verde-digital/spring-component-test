package com.pv.componenttest;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

public class Tests {

    @Test
    public void test() {

        new SpringApplicationBuilder(App.class).run();

    }

}
