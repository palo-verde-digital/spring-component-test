package com.pv.componenttest.environment;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.core.env.ConfigurableEnvironment;

class EnvironmentJDBC {

    private static final String ROOT_CONFIG = "spring.component-test.infrastructure.db";

    private static final String POSTGRES_CONFIG = ROOT_CONFIG + ".postgres";

    private static final String POSTGRES_IMAGE_CONFIG = POSTGRES_CONFIG + ".image";
    private static final String POSTGRES_IMAGE_DEFAULT = "postgres:alpine";

    private static final String POSTGRES_DATABASES_CONFIG = POSTGRES_CONFIG + ".databases";
    private static final String POSTGRES_DATABASES_DEFAULT = "spring.datasource";

    private static final String POSTGRES_SCRIPTS_CONFIG = POSTGRES_CONFIG + ".scripts";

    private static final String POSTGRES_JDBC_PREFIX = "jdbc:postgresql";
    private static final String POSTGRES_JDBC_DRIVER = "org.postgresql.Driver";

    private static Predicate<DataSourceProperties> DB_HAS_POSTGRES_PREFIX = props -> props.getUrl() != null && props.getUrl().startsWith(POSTGRES_JDBC_PREFIX);
    private static Predicate<DataSourceProperties> DB_HAS_POSTGRES_DRIVER = props -> props.getDriverClassName() != null && props.getDriverClassName().equals(POSTGRES_JDBC_DRIVER);

    static Map<String, String> detect(ConfigurableEnvironment environment) {

        var binder = Binder.get(environment);

        var dataSourceProps = loadDataSourceProperties(environment, binder);

        var dataSourceScripts = binder.bind(POSTGRES_SCRIPTS_CONFIG, Bindable.listOf(String.class))
                .orElse(null);

        return null;

    }

    private static List<Map.Entry<String, DataSourceProperties>> loadDataSourceProperties(ConfigurableEnvironment environment, Binder binder) {

        var dataSourcePaths = binder.bind(POSTGRES_DATABASES_CONFIG, Bindable.listOf(String.class))
                .orElse(List.of(POSTGRES_DATABASES_DEFAULT));

        return dataSourcePaths.stream()
                .map(path -> validateDataSourcePath(binder, path))
                .filter(entry -> entry != null)
                .toList();

    }

    private static Map.Entry<String, DataSourceProperties> validateDataSourcePath(Binder binder, String path) {

        var dataSourceProperties = binder.bind(path, DataSourceProperties.class)
                .orElse(null);

        var validDataSourceProperties = dataSourceProperties != null
            && DB_HAS_POSTGRES_PREFIX.or(DB_HAS_POSTGRES_DRIVER).test(dataSourceProperties);

        return validDataSourceProperties
            ? Map.entry(path, dataSourceProperties)
            : null;

    }

}
