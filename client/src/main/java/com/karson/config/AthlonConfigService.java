package com.karson.config;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.karson.api.common.Multimap;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.ResponseObject;
import com.karson.api.listener.Listener;
import com.karson.api.remote.ConfigService;
import com.karson.remote.Remoteclient;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public class AthlonConfigService implements ConfigService {

    //Work work;
    Multimap<String, Listener> listenerMap = Multimap.createSetMultimap();

    Multimap<String, String> configDataMD5 = Multimap.createSetMultimap();

    Remoteclient configClient;

    ScheduledExecutorService executorService;

    ExecutorService executorListener;

    ExecutorService executorAsync;

    Long interval = 0L;

    final ReentrantLock lock = new ReentrantLock();

    final Condition hasListener = lock.newCondition();

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("client_longPolling-timeout-checker-%d")
            .build();

    private ScheduledExecutorService timeoutChecker = new ScheduledThreadPoolExecutor(1, threadFactory);

    public AthlonConfigService(Properties properties) throws RuntimeException {
        configClient = new Remoteclient(properties);
        executorListener = Executors.newSingleThreadExecutor();
        executorAsync = Executors.newSingleThreadExecutor();
        this.executorService = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setName("AthlonConfigServer-Worker");
            t.setDaemon(true);
            return t;
        });
        this.executorService.schedule(() -> {
            executeConfigListenr();
           // executeConfigListenrWithAsync();
        }, 0L, TimeUnit.MILLISECONDS);
        //        work = new Work();
        //        work.setDaemon(true);
        //        new Thread(work,"athlonConfig-work").run();
    }

    public void reSetinterval() {
        interval = 0l;
    }


    private void exeAsync(){
        executorService.schedule(() -> {
            executeConfigListenrWithAsync();
        }, interval, TimeUnit.SECONDS);
    }


    private void executeConfigListenrWithAsync(){
        lock.lock();
        try{
//            while (listenerMap.keySet().size()<=0) {
//                hasListener.await(60L, TimeUnit.SECONDS);
//            }
            Set<String> keys = listenerMap.keySet();
            ListenableFuture<ReplyPayload> configByDataIdIfChangeWithAsync = getConfigByDataIdIfChangeWithAsync(keys);
            ListenableFuture<ReplyPayload> integerListenableFuture = Futures.withTimeout(configByDataIdIfChangeWithAsync, 40L, TimeUnit.SECONDS,
                    timeoutChecker);
            Futures.addCallback(integerListenableFuture, new FutureCallback<ReplyPayload>() {
                @Override
                public void onSuccess(@NullableDecl ReplyPayload responsePayLoad) {
                    switch (responsePayLoad.getCode()) {
                        case 304:
                            System.out.println("304");
                            reSetinterval();
                            exeAsync();
                            break;
                        case 200:
                            Map<String, ResponseObject> payLoadMap = responsePayLoad.getPayloadMapMap();
                            payLoadMap.forEach((key, value) -> {
                                String currentConfigMd5 = value.getConfigNewMD5();
                                if (!configDataMD5.get(key).get().iterator().next().equals(currentConfigMd5)) {
                                    updateDataMd5(key, currentConfigMd5);
                                    notifyListener(value, listenerMap.get(key));
                                }
                            });
                            reSetinterval();
                            exeAsync();
                            break;
                        default:
                            exeAsync();
                             break;
                    }
                }
                @Override
                public void onFailure(Throwable throwable) {
                  //  System.out.println(throwable);
                    interval = interval + 2L;
                    exeAsync();
                }
            }, executorAsync);

        }catch (Exception e){
             e.printStackTrace();
        }finally {
            lock.unlock();
        }
    }



    private void executeConfigListenr() {
        System.out.println("start executeConfigListenr");
        lock.lock();
        try {
            while (listenerMap.keySet().size()<=0) {
                hasListener.await(60L, TimeUnit.SECONDS);
            }
            Set<String> keys = listenerMap.keySet();
            ReplyPayload responsePayLoad = getConfigByDataIdIfChange(keys);
            switch (responsePayLoad.getCode()) {
                case 304:
                    System.out.println("304");
                    reSetinterval();
                    break;
                case 200:
                    Map<String, ResponseObject> payLoadMap = responsePayLoad.getPayloadMapMap();
                    payLoadMap.forEach((key, value) -> {
                        String currentConfigMd5 = value.getConfigNewMD5();
                        if (!configDataMD5.get(key).get().iterator().next().equals(currentConfigMd5)) {
                            updateDataMd5(key, currentConfigMd5);
                            notifyListener(value, listenerMap.get(key));
                        }
                    });
                    reSetinterval();
                    break;
                default:
                    throw new RuntimeException("can not get server response code");
            }
        } catch (Exception e) {
            interval = interval + 2L;
            e.printStackTrace();
        }finally {
            lock.unlock();
            executorService.schedule(() -> {
                executeConfigListenr();
            }, interval, TimeUnit.SECONDS);
        }


    }

    private void notifyListener(ResponseObject value, Optional<Collection<Listener>> listeners) {
        if (listeners.isPresent()) {
            Collection<Listener> listeners1 = listeners.get();
            listeners1.forEach(e -> {
                if (e.getExecutor() != null) {
                    e.getExecutor().execute(() -> {
                        e.receiveConfigInfo(value.getMessage());
                    });
                } else {
                    executorListener.execute(() -> {
                        e.receiveConfigInfo(value.getMessage());
                    });
                }
            });
        } else {

        }

    }

    private void updateDataMd5(String key, String md5) {
        configDataMD5.clearAndPut(key, md5);
    }

    //    private void executeConfigListenr() {
    //        try{
    //            Set<String> keys = listenerMap.keySet();
    //            for(String dataId:keys){
    //                Optional<Collection<Listener>> listeners = listenerMap.get(dataId);
    //                if(listeners.isPresent()){
    //                    Collection<Listener> listeners1 = listeners.get();
    //                    ReplyPayload responsePayLoad = getConfigByDataIdIfChange(dataId);
    //                    switch (responsePayLoad.getCode()){
    //                        case 304:
    //                            break;
    //                        case 200:
    //                            String message = responsePayLoad.getMessage();
    //                            for (Listener listener : listeners1) {
    //                                Executor executor = listener.getExecutor();
    //                                if(null!=executor){
    //                                    executor.execute(()->{
    //                                        listener.receiveConfigInfo(message);
    //                                    });
    //                                }else{
    //                                    executorListener.execute(()->{
    //                                        listener.receiveConfigInfo(message);
    //                                    });
    //                                }
    //                            }
    //                            break;
    //                        default:
    //                            throw new RuntimeException("can not get server response code");
    //                    }
    //                }
    //
    //            }
    //
    //        }catch (Exception e){
    //
    //        }finally {
    //            executorService.schedule(()->{
    //                executeConfigListenr();
    //            },0L, TimeUnit.SECONDS);
    //        }
    //
    //    }

    private ReplyPayload getConfigByDataIdIfChange(Set<String> dataId) {
        dataId.forEach(e -> {
            configDataMD5.putIfAbsent(e,"");
        });
        return configClient.getConfigByDataIdIfChange(dataId, configDataMD5, 40L);
    }

    private ListenableFuture<ReplyPayload> getConfigByDataIdIfChangeWithAsync(Set<String> dataId) {
        dataId.forEach(e -> {
            configDataMD5.putIfAbsent(e,"");
        });
        ListenableFuture<ReplyPayload> configByDataIdIfChangeWithAsync = configClient.getConfigByDataIdIfChangeWithAsync(
                dataId, configDataMD5);
        return configByDataIdIfChangeWithAsync;
    }

    @Override
    public String getConfig(String dataId, long timeoutMs) throws RuntimeException {
        // dataVersion.putIfAbsent(dataId,0L);
        return null;
    }

    @Override
    public void addListener(String dataId, Listener listener) throws RuntimeException {
        lock.lock();
        try {
            configDataMD5.putIfAbsent(dataId, "");
            listenerMap.put(dataId, listener);
            hasListener.signalAll();
        }finally {
            lock.unlock();
        }

    }
    //
    //    class Work extends Thread{
    //
    //        @Override
    //        public void run() {
    //
    //        }
    //    }


}
