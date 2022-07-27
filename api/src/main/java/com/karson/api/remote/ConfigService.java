package com.karson.api.remote;

import com.karson.api.listener.Listener;

public interface ConfigService {
    String getConfig(String dataId) throws RuntimeException;
    void addListener(String dataId, Listener listener) throws RuntimeException;
    boolean publishConfig(String dataId, String config) throws RuntimeException;
}
