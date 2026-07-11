package com.pv.componenttest.environment;

import java.util.Arrays;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import com.pv.componenttest.environment.option.LoadOption;

@Order
public class ComponentTestPostProcessor implements EnvironmentPostProcessor {

    private final ResourceLoader resourceLoader = new DefaultResourceLoader();
    private final Log logger;

    private static final String ROOT_CONFIG = "spring.component-test";

    private static final String INFRASTRUCTURE_CONFIG = ROOT_CONFIG + ".infrastructure";

    private static final String INFRASTRUCTURE_PATH_CONFIG = INFRASTRUCTURE_CONFIG + ".path";
    private static final String INFRASTRUCTURE_PATH_DEFAULT = "docker/compose.yaml";

    private static final String INFRASTRUCTURE_LOAD_CONFIG = INFRASTRUCTURE_CONFIG + ".load";
    private static final String INFRASTRUCTURE_LOAD_DEFAULT = LoadOption.ALWAYS.name();

    private static final Map<Class<? extends Enum<?>>, String> configPaths = Map.ofEntries(
        Map.entry(LoadOption.class, INFRASTRUCTURE_LOAD_CONFIG)
    );

    public ComponentTestPostProcessor(DeferredLogFactory logFactory) {
        logger = logFactory.getLog(getClass());
    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        loadInfrastructureDefinition(environment);

    }

    private Resource loadInfrastructureDefinition(ConfigurableEnvironment environment) {

        var infrastructurePath = environment.getProperty(INFRASTRUCTURE_PATH_CONFIG, INFRASTRUCTURE_PATH_DEFAULT);
        var infrastructureDefinition = resourceLoader.getResource(infrastructurePath);

        if (!infrastructureDefinition.exists()) {
            logger.info("No infrastructure definition found at '" + infrastructurePath + "'. Infrastructure definition will be created.");

            return createInfrastructureDefinition(environment);
        }

        var infrastructureLoadProperty = environment.getProperty(INFRASTRUCTURE_LOAD_CONFIG, INFRASTRUCTURE_LOAD_DEFAULT);
        var infrastructureLoad = loadConfig(infrastructureLoadProperty, LoadOption.class, LoadOption.ALWAYS);

        return switch (infrastructureLoad) {
           	case ALWAYS -> createInfrastructureDefinition(environment);
            case ON_UPDATE -> verifyInfrastructureDefinition(environment, infrastructureDefinition);
        };

    }

    private Resource createInfrastructureDefinition(ConfigurableEnvironment environment) {

        return null;

    }

    private Resource verifyInfrastructureDefinition(ConfigurableEnvironment environment, Resource infrastructureDefinition) {

        return infrastructureDefinition;

    }

    private <T extends Enum<T>> T loadConfig(String propertyValue, Class<T> valueSet, T defaultValue) {
        var configValue = Arrays.stream(valueSet.getEnumConstants())
                .filter(value -> value.name().equalsIgnoreCase(propertyValue))
                .findFirst()
                .orElse(null);

        if(configValue == null) {
            logConfigError(configPaths.get(valueSet), propertyValue, defaultValue.name());

            return defaultValue;
        }

        return configValue;
    }

    private void logConfigError(String configPath, String providedValue, String fallbackValue) {

        logger.info("Invalid value '" + providedValue + "' detected at '" + configPath + "'. Will use default value of '" + fallbackValue +"'");

    }

}
