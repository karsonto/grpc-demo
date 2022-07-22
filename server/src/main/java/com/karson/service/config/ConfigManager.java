package com.karson.service.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.karson.api.common.Multimap;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestObject;
import com.karson.api.grpc.RequestPayload;
import com.karson.api.remote.ConfigServerService;
import com.karson.service.suspension.AsyncContext;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class ConfigManager implements ConfigServerService {

    Multimap<String, String> configContainer = Multimap.createSetMultimap();

    Multimap<String, Long> configVersionContainer = Multimap.createSetMultimap();

    Multimap<Set<String>, AsyncContext> asyncContextContainer = Multimap.createSetMultimap();

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("longPolling-timeout-checker-%d")
            .build();

    private ScheduledExecutorService timeoutChecker = new ScheduledThreadPoolExecutor(1, threadFactory);

    @Override
    public void publishConfig(String dataId, String config) throws RuntimeException {

    }

    @Override
    public String getConfig(String dataId) throws RuntimeException {
        Set<String> set = new HashSet<>(Arrays.asList(dataId));
        return getConfigs(set).get(dataId);
    }

    @Override
    public Map<String, String> getConfigs(Set<String> dataIds) throws RuntimeException {
        return null;
    }

    public void receive(AsyncContext asyncContext) {
        RequestPayload request = asyncContext.getRequest();
        Map<String, RequestObject> payloadMapMap = request.getPayloadMapMap();
        Set<String> dataIds = payloadMapMap.keySet();
        asyncContextContainer.put(dataIds, asyncContext);
        timeoutChecker.schedule(() -> {
            if (asyncContext.isTimeOut()) {
                asyncContextContainer.remove(dataIds, asyncContext);
                StreamObserver<ReplyPayload> responseObserver = asyncContext.getResponseObserver();
                ReplyPayload replyPayload = ReplyPayload.newBuilder().setResponseId(request.getRequestId()).setCode(302)
                        .build();
                //输出响应
                responseObserver.onNext(replyPayload);
                //结束响应
                responseObserver.onCompleted();
            }
        }, 25, TimeUnit.SECONDS);
    }
}
