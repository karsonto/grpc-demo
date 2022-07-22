package com.karson.api.remote;

import java.util.Map;
import java.util.Set;

public interface ConfigServerService {
    void publishConfig(String dataId,String config) throws RuntimeException;
    String getConfig(String dataId) throws RuntimeException;
    Map<String,String> getConfigs(Set<String> dataIds) throws RuntimeException;
}
