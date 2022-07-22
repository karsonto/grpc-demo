package com.karson.config;

import com.karson.api.remote.ConfigService;

import java.lang.reflect.Constructor;
import java.util.Properties;

public class ConfigFactory {
    public static ConfigService createConfigService(Properties properties) throws RuntimeException {
        try {
            Class<?> driverImplClass = Class.forName("com.karson.config.AthlonConfigService");
            Constructor constructor = driverImplClass.getConstructor(Properties.class);
            ConfigService vendorImpl = (ConfigService) constructor.newInstance(properties);
            return vendorImpl;
        } catch (Throwable e) {
            throw new RuntimeException("Create athlonConfigService Fail", e);
        }
    }
}
