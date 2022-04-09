package com.karson.service.impl;

import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestGrpc;
import com.karson.api.grpc.RequestPayload;
import io.grpc.stub.StreamObserver;


public class GrpcServiceImpl extends RequestGrpc.RequestImplBase {
    
    @Override
    public void request(RequestPayload request, StreamObserver<ReplyPayload> responseObserver) {
        System.out.println(request.getMessage());
        ReplyPayload replyPayload = ReplyPayload.newBuilder()
                .setMessage("Hello " + request.getMessage())
                .build();
        //输出响应
        responseObserver.onNext(replyPayload);
        //结束响应
        responseObserver.onCompleted();
    }
}
