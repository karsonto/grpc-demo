package com.karson.service.impl;

import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestGrpc;
import com.karson.api.grpc.RequestPayload;
import com.karson.service.config.ConfigManager;
import com.karson.service.suspension.AsyncContext;
import io.grpc.stub.StreamObserver;


public class GrpcServiceImpl extends RequestGrpc.RequestImplBase {
    ConfigManager configManager = new ConfigManager();
    
    @Override
    public void request(RequestPayload request, StreamObserver<ReplyPayload> responseObserver) {
        AsyncContext asyncContext = new AsyncContext(request, responseObserver);
        configManager.receive(asyncContext);
        //        System.out.println(request.getMessage());
//        ReplyPayload replyPayload = ReplyPayload.newBuilder()
//                .setMessage("Hello " + request.getMessage())
//                .build();
        //输出响应
       // responseObserver.onNext(replyPayload);
        //结束响应
 //       responseObserver.onCompleted();
    }
    
    @Override
    public StreamObserver<RequestPayload> requestBiStream(StreamObserver<ReplyPayload> responseObserver) {
//        //实现StreamObserver接受客户端流
//        return new StreamObserver<RequestPayload>() {
//            @Override
//            public void onNext(RequestPayload request) {
//                //接受请求
//                System.out.printf("[BiStream]recv: %s\n", request.getMessage());
//                //构造返回结果
//                ReplyPayload helloReply = ReplyPayload.newBuilder()
//                        .setMessage("Hello " + request.getMessage())
//                        .build();
//                System.out.printf("[BiStream]resp: %s", helloReply);
//                //输出响应
//                responseObserver.onNext(helloReply);
//            }
//
//            @Override
//            public void onError(Throwable t) {
//                //处理错误
//                System.err.println("[BiStream]recv error!");
//                t.printStackTrace();
//            }
//
//            @Override
//            public void onCompleted() {
//                System.out.println("[BiStream]resp completed!");
//                //结束响应
//                responseObserver.onCompleted();
//            }
//        };
//
        return null;
   }
}
