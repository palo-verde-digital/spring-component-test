package com.pv.componenttest.environment;

import static com.pv.componenttest.environment.ComponentTestPostProcessor.ROOT_CONFIG;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.core.env.ConfigurableEnvironment;

class EnvironmentJDBC {

    static final String DB_CONFIG = ROOT_CONFIG + ".db";

    private static final String POSTGRES_CONFIG = DB_CONFIG + ".postgres";

    private static final String POSTGRES_IMAGE_CONFIG = POSTGRES_CONFIG + ".image";
    private static final String POSTGRES_IMAGE_DEFAULT = "classpath:postgres:alpine";

    private static final String POSTGRES_DATABASES_CONFIG = POSTGRES_CONFIG + ".databases";
    private static final String POSTGRES_DATABASES_DEFAULT = "spring.datasource";

    private static final String POSTGRES_SCRIPTS_CONFIG = POSTGRES_CONFIG + ".scripts";

    private static final String POSTGRES_JDBC_PREFIX = "jdbc:postgresql";
    private static final String POSTGRES_JDBC_DRIVER = "org.postgresql.Driver";

    private static Predicate<DataSourceProperties> DB_HAS_POSTGRES_PREFIX = props -> props.getUrl() != null && props.getUrl().startsWith(POSTGRES_JDBC_PREFIX);
    private static Predicate<DataSourceProperties> DB_HAS_POSTGRES_DRIVER = props -> props.getDriverClassName() != null && props.getDriverClassName().equals(POSTGRES_JDBC_DRIVER);

    static Map<String, Object> detect(ConfigurableEnvironment environment) {

        var binder = Binder.get(environment);

        var dataSourceProps = loadDataSourceProperties(binder);
        var dataSourceScripts = loadDataSourceScripts(binder);

        if (dataSourceProps == null && dataSourceScripts == null) {
            return null;
        }

        var jdbcEnvironment = new HashMap<String, Object>();

        for (var props: dataSourceProps) {
            var dataSourceName = props.getName();

            jdbcEnvironment.put(dataSourceName + ".script_name", scriptFor(dataSourceScripts, dataSourceName));
            jdbcEnvironment.put(dataSourceName + ".user", props.getUsername());
            jdbcEnvironment.put(dataSourceName + ".password", props.getPassword());
            jdbcEnvironment.put(dataSourceName + ".type", "postgres");
        }

        jdbcEnvironment.put("postgres.image", environment.getProperty(POSTGRES_IMAGE_CONFIG, POSTGRES_IMAGE_DEFAULT));

        return jdbcEnvironment;

    }

    private static List<DataSourceProperties> loadDataSourceProperties(Binder binder) {

        var dataSourcePaths = binder.bind(POSTGRES_DATABASES_CONFIG, Bindable.listOf(String.class))
                .orElse(List.of(POSTGRES_DATABASES_DEFAULT));

        var dataSourceProperties = dataSourcePaths.stream()
                .map(path -> dataSourcePropertiesForPath(binder, path))
                .filter(entry -> entry != null)
                .toList();

        return dataSourceProperties.isEmpty()
            ? null
            : dataSourceProperties;

    }

    private static List<String> loadDataSourceScripts(Binder binder) {

        var dataSourceScripts = binder.bind(POSTGRES_SCRIPTS_CONFIG, Bindable.listOf(String.class))
                .orElse(null);

        return dataSourceScripts == null || dataSourceScripts.isEmpty()
            ? null
            : dataSourceScripts;

    }

    private static DataSourceProperties dataSourcePropertiesForPath(Binder binder, String path) {

        var dataSourceProperties = binder.bind(path, DataSourceProperties.class)
                .orElse(null);

        var validDataSourceProperties = dataSourceProperties != null
            && DB_HAS_POSTGRES_PREFIX.or(DB_HAS_POSTGRES_DRIVER).test(dataSourceProperties);

        return validDataSourceProperties
            ? dataSourceProperties
            : null;

    }

    private static String scriptFor(List<String> scripts, String databaseName) {

        var matchingScripts = scripts.stream()
                .map(script -> Paths.get(script).getFileName().toString())
                .map(scriptFile -> scriptFile.split("\\.")[0])
                .filter(scriptName -> scriptName.equals(databaseName))
                .toList();

        if (matchingScripts.size() != 1) {
            throw new IllegalStateException("No initialization script detected for database '" + databaseName + "'");
        }

        return matchingScripts.getFirst();

    }

}
