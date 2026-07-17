package com.pv.componenttest.infrastructure.db;

import static com.pv.componenttest.environment.ComponentTestPostProcessor.ROOT_CONFIG;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;

public class DB {

    static class DBServiceDescriptor {

        String image;

        List<String> databases;
        List<String> scripts;

    }

    static final String DB_CONFIG = ROOT_CONFIG + ".db";

    static final String DB_POSTGRES = "postgres";

    public static Map<String, Object> detect(ConfigurableEnvironment environment, Log logger) {

        var binder = Binder.get(environment);
        var dbServiceDescriptors = binder.bind(DB_CONFIG, Bindable.mapOf(String.class, DBServiceDescriptor.class))
                .orElse(null);

        if (dbServiceDescriptors == null) {
            return null;
        }

        var dbServices = new HashMap<String, Object>();

        for(var dbServiceDescriptor: dbServiceDescriptors.entrySet()) {
            var dbService = configureService(dbServiceDescriptor);

            if (dbService != null) {
                dbServices.put(dbServiceDescriptor.getKey(), dbService);
            }
        }

        return dbServices;

    }

    private static Map<String, Object> configureService(Entry<String, DBServiceDescriptor> dbServiceDescriptor) {

        return switch(dbServiceDescriptor.getKey()) {
            case DB_POSTGRES -> DBPostgres.writeService(dbServiceDescriptor.getValue());
            default -> null;
        };

    }

}
