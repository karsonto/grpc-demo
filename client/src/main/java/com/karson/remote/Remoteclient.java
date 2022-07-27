package com.karson.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestObject;
import com.karson.api.grpc.RequestPayload;
import com.karson.grpc.GrpcClient;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

public class Remoteclient {

    static AtomicLong REQUEST_ID = new AtomicLong(0L);

    GrpcClient grpcClient;

    public Remoteclient(Properties properties) {
        grpcClient = new GrpcClient();
        String serverIp = properties.getProperty("config-server-ip");
        if (null == serverIp) {
            serverIp = "localhost:9999";
        }
        grpcClient.init(serverIp);
    }

    public ReplyPayload getConfigByDataIdIfChange(Set<String> dataIds, Map<String,String> configDataMD5,
            Long timeOut) {
        RequestPayload payLoadRequest = buildRequestPayload(dataIds, configDataMD5,timeOut);
        return grpcClient.requestBlocking(payLoadRequest, timeOut);
    }

    public  ListenableFuture<ReplyPayload> getConfigByDataIdIfChangeWithAsync(Set<String> dataIds, Map<String, String> configDataMD5,Long timeOut) {
        RequestPayload payLoadRequest = buildRequestPayload(dataIds, configDataMD5,timeOut);
        ListenableFuture<ReplyPayload> replyPayloadListenableFuture = grpcClient.requestFuture(payLoadRequest);
        return replyPayloadListenableFuture;
    }


    private RequestPayload buildRequestPayload(Set<String> dataIds, Map <String, String> configDataMD5,Long timeOut) {
        Map<String, RequestObject> requestObjectMap = new HashMap<>();
        dataIds.forEach(e -> {
            String configMd5 = configDataMD5.get(e);
            requestObjectMap.put(e, RequestObject.newBuilder().setConfigOldMD5(configMd5).build());
        });
        RequestPayload payLoadRequest = RequestPayload.newBuilder().setRequestId(REQUEST_ID.getAndIncrement()).setTimeout(timeOut).setPublish(1)
                .putAllPayloadMap(requestObjectMap).build();
        return payLoadRequest;
    }


    public boolean publishConfig(String dataId, String config, long timeOut) {
        Map<String, RequestObject> requestObjectMap = new HashMap<>();
        requestObjectMap.put(dataId, RequestObject.newBuilder().setConfigOldMD5(config).build());
        RequestPayload payLoadRequest = RequestPayload.newBuilder().setPublish(2).setRequestId(REQUEST_ID.getAndIncrement()).setTimeout(timeOut).setPublish(2)
                .putAllPayloadMap(requestObjectMap).build();
        ReplyPayload replyPayload = grpcClient.requestBlocking(payLoadRequest, timeOut);
        System.out.println("receive publishConfig response Id : " +  replyPayload.getResponseId());
        return replyPayload.getCode() ==200;
    }
}
