package com.bbva.arq.devops.ae.mirrorgate.jenkins.plugin.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

/**
 * Custom object mapper that knows how to serialize Mongo ObjectIds.
 */
public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        SimpleModule module = new SimpleModule("ObjectIdModule");
        this.registerModule(module);
    }
}
