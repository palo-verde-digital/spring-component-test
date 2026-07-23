package com.pv.componenttest.infrastructure.db;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.logging.Log;

import com.pv.componenttest.infrastructure.db.DB.DBServiceDescriptor;

class DBPostgres {

    private static final String POSTGRES_IMAGE = "postgres:alpine";
    private static final String POSTGRES_CONTAINER = "postgres_container";
    private static final String POSTGRES_RESTART = "always";

    private static final Path SCRIPT_ORIGIN = Path.of("target/test-classes/sql");
    private static final Path SCRIPT_DESTINATION = Path.of("target/test-classes/sql/postgres");

    private static final List<String> POSTGRES_PORTS = List.of("5432:5432");

    private static final Map<String, String> POSTGRES_ADMIN = Map.ofEntries(
        Map.entry("POSTGRES_USER", "postgres"),
        Map.entry("POSTGRES_PASSWORD", "postgres")
    );

    static Map<String, Object> writeService(DBServiceDescriptor dbServiceDescriptor, Log logger) {

        logger.info("Creating postgres DB service");

        var postgresService = new HashMap<String, Object>();

        var image = Optional.ofNullable(dbServiceDescriptor)
                .map(DBServiceDescriptor::image)
                .orElse(POSTGRES_IMAGE);

        postgresService.put("image", image);
        postgresService.put("container_name", POSTGRES_CONTAINER);
        postgresService.put("restart", POSTGRES_RESTART);
        postgresService.put("environment", POSTGRES_ADMIN);
        postgresService.put("ports", POSTGRES_PORTS);

        return postgresService;

    }

}
