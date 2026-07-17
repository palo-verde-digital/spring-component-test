package com.pv.componenttest.environment;

import static org.yaml.snakeyaml.DumperOptions.FlowStyle.BLOCK;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.pv.componenttest.infrastructure.db.DB;

@Order
public class ComponentTestPostProcessor implements EnvironmentPostProcessor {

    private final Log logger;

    public static final String ROOT_CONFIG = "spring.component-test";

    private static final Path COMPOSE_LOCATION = Path.of("target/test-classes/docker/compose.yaml");

    public ComponentTestPostProcessor(DeferredLogFactory logFactory) {

        logger = logFactory.getLog(getClass());

    }

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        var jdbcEnvironment = DB.detect(environment, logger);
        if(jdbcEnvironment != null) {
            logger.info("Detected JDBC environment: " + jdbcEnvironment.toString());
        } else {
            logger.info("No JDBC definitions detected.");
        }

        try {
            writeCompose(jdbcEnvironment);
        } catch (IOException e) {
            logger.error("Unable to write compose file: " + e.getMessage());
        }

    }

    private void writeCompose(Map<String, Object> composeContents) throws IOException {

        Files.deleteIfExists(COMPOSE_LOCATION);
        Files.createDirectories(COMPOSE_LOCATION.getParent());
        Files.createFile(COMPOSE_LOCATION);

        var composeYaml = configureYaml();
        composeYaml.dump(composeContents, new FileWriter(COMPOSE_LOCATION.toFile()));

    }

    private Yaml configureYaml() {

        var options = new DumperOptions();

        options.setDefaultFlowStyle(BLOCK);
        options.setPrettyFlow(true);
        options.setIndent(2);

        var compose = new Yaml(options);

        return compose;

    }

}
