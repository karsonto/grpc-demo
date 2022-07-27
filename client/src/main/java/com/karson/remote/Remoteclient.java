package com.karson.remote;

import com.google.common.util.concurrent.ListenableFuture;
import com.karson.api.common.Multimap;
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

    public ReplyPayload getConfigByDataIdIfChange(Set<String> dataIds, Multimap<String, String> configDataMD5,
            Long timeOut) {
        RequestPayload payLoadRequest = buildRequestPayload(dataIds, configDataMD5,timeOut);
        return grpcClient.requestBlocking(payLoadRequest, timeOut);
    }

    public  ListenableFuture<ReplyPayload> getConfigByDataIdIfChangeWithAsync(Set<String> dataIds, Multimap<String, String> configDataMD5,Long timeOut) {
        RequestPayload payLoadRequest = buildRequestPayload(dataIds, configDataMD5,timeOut);
        ListenableFuture<ReplyPayload> replyPayloadListenableFuture = grpcClient.requestFuture(payLoadRequest);
        return replyPayloadListenableFuture;
    }


    private RequestPayload buildRequestPayload(Set<String> dataIds, Multimap<String, String> configDataMD5,Long timeOut) {
        Map<String, RequestObject> requestObjectMap = new HashMap<>();
        dataIds.forEach(e -> {
            String configMd5 = configDataMD5.get(e).get().iterator().next();
            requestObjectMap.put(e, RequestObject.newBuilder().setConfigOldMD5(configMd5).build());
        });
        RequestPayload payLoadRequest = RequestPayload.newBuilder().setRequestId(REQUEST_ID.getAndIncrement()).setTimeout(timeOut)
                .putAllPayloadMap(requestObjectMap).build();
        return payLoadRequest;
    }


}
