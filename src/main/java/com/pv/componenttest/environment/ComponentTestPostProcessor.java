package com.pv.componenttest.environment;

import org.apache.commons.logging.Log;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;

@Order
public class ComponentTestPostProcessor implements EnvironmentPostProcessor {

    private final Log logger;

    public ComponentTestPostProcessor(DeferredLogFactory logFactory) {

        logger = logFactory.getLog(getClass());

    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        var jdbcEnvironment = EnvironmentJDBC.detect(environment);
        if(jdbcEnvironment != null) {
            logger.info("Detected JDBC environment: " + jdbcEnvironment.toString());
        } else {
            logger.info("No JDBC definitions detected.");
        }

    }

}
