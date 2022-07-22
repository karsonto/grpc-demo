package com.karson.api.listener;

import java.util.concurrent.Executor;

public interface Listener {
    void receiveConfigInfo(final String configInfo);
    Executor getExecutor();
}
