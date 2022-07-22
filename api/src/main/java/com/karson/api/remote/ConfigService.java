package com.karson.api.remote;

import com.karson.api.listener.Listener;

public interface ConfigService {
    String getConfig(String dataId, long timeoutMs) throws RuntimeException;
    void addListener(String dataId, Listener listener) throws RuntimeException;

}
