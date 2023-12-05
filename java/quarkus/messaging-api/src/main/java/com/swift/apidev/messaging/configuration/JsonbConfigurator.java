package com.swift.apidev.messaging.configuration;

import io.quarkus.jsonb.JsonbConfigCustomizer;

import jakarta.inject.Singleton;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.BinaryDataStrategy;

/**
 * The JsonbConfigurator configures the binary data strategy of JSON Binding. Instead of the default
 * strategy "byte array" the "base 64" encoding is activated.
 */
@Singleton
public class JsonbConfigurator implements JsonbConfigCustomizer {
    @Override
    public void customize(JsonbConfig jsonbConfig) {
        jsonbConfig.withNullValues(false);
        jsonbConfig.withBinaryDataStrategy(BinaryDataStrategy.BASE_64);
    }
}
