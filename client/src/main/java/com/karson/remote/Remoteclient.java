package com.karson.remote;

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
        grpcClient.init(serverIp);
    }

    public ReplyPayload getConfigByDataIdIfChange(Set<String> dataIds, Multimap<String, Long> dataVersion,Long timeOut) {
        Map<String, RequestObject> requestObjectMap = new HashMap<>();
        dataIds.forEach(e->{
            Long oldVersion = dataVersion.get(e).get().iterator().next();
            requestObjectMap.put(e, RequestObject.newBuilder().setOldVersion(oldVersion).build());
        });
        RequestPayload payLoadRequest = RequestPayload.newBuilder().setRequestId(REQUEST_ID.getAndIncrement())
                .putAllPayloadMap(requestObjectMap)
                .build();
       return grpcClient.requestBlocking(payLoadRequest,timeOut);
    }
}
