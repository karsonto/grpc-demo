package com.karson.config;

import com.karson.api.common.Multimap;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.ResponseObject;
import com.karson.api.listener.Listener;
import com.karson.api.remote.ConfigService;
import com.karson.remote.Remoteclient;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AthlonConfigService implements ConfigService {

    //Work work;
    Multimap<String, Listener> listenerMap = Multimap.createSetMultimap();

    Multimap<String,Long> dataVersion = Multimap.createSetMultimap();

    Remoteclient configClient;

    ScheduledExecutorService executorService;

    ExecutorService executorListener;

    Long interval = 0L;

    public AthlonConfigService(Properties properties) throws RuntimeException {
        configClient = new Remoteclient(properties);
        executorListener = Executors.newSingleThreadExecutor();
        this.executorService = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r);
            t.setName("AthlonConfigServer-Worker");
            t.setDaemon(true);
            return t;
        });
        this.executorService.schedule(()->{
            executeConfigListenr();
        },0L, TimeUnit.MILLISECONDS);
        //        work = new Work();
        //        work.setDaemon(true);
        //        new Thread(work,"athlonConfig-work").run();
    }

    public void reSetinterval(){
        interval = 0l;
    }

    private void executeConfigListenr() {
        try{
            Set<String> keys = listenerMap.keySet();
            ReplyPayload responsePayLoad  = getConfigByDataIdIfChange(keys);
            switch (responsePayLoad.getCode()){
                case 304:
                    reSetinterval();
                    break;
                case 200:
                    Map<String, ResponseObject> payLoadMap = responsePayLoad.getPayloadMapMap();
                    payLoadMap.forEach((key,value)->{
                        long currentVersion = value.getCurrentVersion();
                        if(dataVersion.get(key).get().iterator().next() < currentVersion){
                            updateDataVersion(key,currentVersion);
                            notifyListener(value,listenerMap.get(key));
                        }
                    });
                    reSetinterval();
                    break;
                default:
                    throw new RuntimeException("can not get server response code");
            }
        }catch (Exception e){
            interval = interval + 2L;
            e.printStackTrace();
        }finally {
            executorService.schedule(()->{
                executeConfigListenr();
            },interval, TimeUnit.SECONDS);
        }

    }

    private void notifyListener(ResponseObject value, Optional<Collection<Listener>> listeners) {
        if(listeners.isPresent()){
            Collection<Listener> listeners1 = listeners.get();
            listeners1.forEach(e->{
                if(e.getExecutor()!=null){
                    e.getExecutor().execute(()->{
                        e.receiveConfigInfo(value.getMessage());
                    });
                }else{
                    executorListener.execute(()->{
                        e.receiveConfigInfo(value.getMessage());
                    });
                }
            });
        }else{

        }

    }

    private void updateDataVersion(String key, long currentVersion) {
        dataVersion.clearAndPut(key,currentVersion);
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
        dataId.forEach(e->{
            dataVersion.putIfAbsent(e,0L);
        });
        return configClient.getConfigByDataIdIfChange(dataId,dataVersion,30L);
    }

    @Override
    public String getConfig(String dataId, long timeoutMs) throws RuntimeException {
       // dataVersion.putIfAbsent(dataId,0L);
        return null;
    }

    @Override
    public void addListener(String dataId, Listener listener) throws RuntimeException {
        dataVersion.putIfAbsent(dataId, 0L);
        listenerMap.put(dataId, listener);
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
