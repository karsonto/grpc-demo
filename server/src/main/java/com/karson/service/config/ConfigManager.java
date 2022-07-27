package com.karson.service.config;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.karson.api.common.MD5Utils;
import com.karson.api.common.Multimap;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestObject;
import com.karson.api.grpc.RequestPayload;
import com.karson.api.grpc.ResponseObject;
import com.karson.api.remote.ConfigServerService;
import com.karson.service.suspension.AsyncContext;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ConfigManager implements ConfigServerService {

    Multimap<String, String> configContainer = Multimap.createSetMultimap();

    Multimap<String, String> configMd5Container = Multimap.createSetMultimap();

    private ReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    Multimap<Set<String>, AsyncContext> asyncContextContainer = Multimap.createSetMultimap();

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("longPolling-timeout-checker-%d")
            .build();

    private ScheduledExecutorService timeoutChecker = new ScheduledThreadPoolExecutor(1, threadFactory);

    private ExecutorService notifyThreadPool = Executors.newCachedThreadPool();
    @Override
    public void publishConfig(String dataId, String config) throws RuntimeException {
        Lock lock = readWriteLock.writeLock();
        try{
            lock.lock();
            configContainer.put(dataId,config);
            configMd5Container.put(dataId, MD5Utils.md5Hex(config,"utf-8"));
        }finally {
            lock.unlock();
        }
        notifyAsyncContext(dataId);
    }

    private void notifyAsyncContext(String dataId) {
        Set<Set<String>> sets = asyncContextContainer.keySet();
        for(Set<String> keys:sets){
            if(keys.contains(dataId)){
                Optional<Collection<AsyncContext>> asyncContexts = asyncContextContainer.get(keys);
                if(asyncContexts.isPresent()){
                    Collection<AsyncContext> asyncContexts1 = asyncContexts.get();
                    Iterator<AsyncContext> iterator = asyncContexts1.iterator();
                    while (iterator.hasNext()){
                        AsyncContext asyncContext = iterator.next();
                        notifyThreadPool.execute(()->{
                            asyncContext.execByLock((e->{
                                e.setTimeOut(false);
                                RequestPayload request = e.getRequest();
                                Map<String, RequestObject> payloadMapMap = request.getPayloadMapMap();
                                Set<String> dataIds = payloadMapMap.keySet();
                                StreamObserver<ReplyPayload> responseObserver = e.getResponseObserver();
                                Map<String, String> configs = getConfigs(dataIds);
                                Map<String,ResponseObject> result = new HashMap<>();
                                configs.forEach((key,value)->{
                                    RequestObject requestObject = payloadMapMap.get(key);
                                    String configOldMD5 = requestObject.getConfigOldMD5();
                                    String newMd5 = MD5Utils.md5Hex(value,"utf-8") ;
                                    ResponseObject responseObject = ResponseObject.newBuilder().setConfigNewMD5(newMd5).setMessage(configOldMD5.equals(newMd5)?null:value).build();
                                    result.put(key,responseObject);
                                });
                                ReplyPayload replyPayload = ReplyPayload.newBuilder().setResponseId(request.getRequestId()).setCode(200).putAllPayloadMap(result)
                                        .build();
                                //输出响应
                                responseObserver.onNext(replyPayload);
                                //结束响应
                                responseObserver.onCompleted();
                            }));
                        });
                        iterator.remove();
                     //   asyncContextContainer.remove(keys,asyncContext);

                    }


                }

            }

        }

    }

    @Override
    public String getConfig(String dataId) throws RuntimeException {
        Set<String> set = new HashSet<>(Arrays.asList(dataId));
        return getConfigs(set).get(dataId);
    }

    @Override
    public Map<String, String> getConfigs(Set<String> dataIds) throws RuntimeException {
        Lock lock = readWriteLock.readLock();
        Map<String, String> result = new HashMap<>();
        try{
            lock.lock();
            dataIds.forEach(e->{
                result.put(e,configContainer.get(e).get().iterator().next());
            });
        }finally {
            lock.unlock();
        }
        return null;
    }

    public void receive(AsyncContext asyncContext) {
        RequestPayload request = asyncContext.getRequest();
        long timeout = request.getTimeout();
        Map<String, RequestObject> payloadMapMap = request.getPayloadMapMap();
        Set<String> dataIds = payloadMapMap.keySet();
        if(timeout == 0L){
            StreamObserver<ReplyPayload> responseObserver = asyncContext.getResponseObserver();
            Map<String, String> configs = getConfigs(dataIds);
            Map<String,ResponseObject> result = new HashMap<>();
            configs.forEach((key,value)->{
                ResponseObject responseObject = ResponseObject.newBuilder().setConfigNewMD5(MD5Utils.md5Hex(value,"utf-8")).setMessage(value).build();
                result.put(key,responseObject);
            });
            ReplyPayload replyPayload = ReplyPayload.newBuilder().setResponseId(request.getRequestId()).setCode(200).putAllPayloadMap(result)
                    .build();
            //输出响应
            responseObserver.onNext(replyPayload);
            //结束响应
            responseObserver.onCompleted();
            return;
        }
        asyncContextContainer.put(dataIds,asyncContext);
        timeoutChecker.schedule(() -> {
            asyncContext.execByLock(e->{
                if (e.isTimeOut()) {
                    asyncContextContainer.remove(dataIds, e);
                    StreamObserver<ReplyPayload> responseObserver = e.getResponseObserver();
                    ReplyPayload replyPayload = ReplyPayload.newBuilder().setResponseId(request.getRequestId()).setCode(304)
                            .build();
                    //输出响应
                    responseObserver.onNext(replyPayload);
                    //结束响应
                    responseObserver.onCompleted();
                }
            });

        }, timeout - 3L, TimeUnit.SECONDS);
    }
}
