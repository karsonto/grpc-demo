package com.karson.grpc;

import com.google.common.util.concurrent.ListenableFuture;
import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestGrpc;
import com.karson.api.grpc.RequestPayload;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class GrpcClient {
    private RequestGrpc.RequestBlockingStub requestBlockingStub;
    private RequestGrpc.RequestStub requestStub;
    private RequestGrpc.RequestFutureStub requestFutureStub;
    private ManagedChannel managedChannel;

    //    public static void main(String[] args) throws InterruptedException {
//        String target = "localhost:9090";
//        //初始化client stub
//        GrpcClient grpcClient = new GrpcClient();
//        grpcClient.init(target);
//        RequestPayload helloRequest = RequestPayload.newBuilder()
//                .setMessage("Karson").setId(1L)
//                .build();
//        while (true) {
//            try {
//                ReplyPayload replyPayload = grpcClient.requestBlocking(helloRequest);
//                System.out.println(replyPayload.getMessage());
//                Thread.sleep(100);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//
//       grpcClient.requestNonBlocking(helloRequest);
//        grpcClient.requestFuture(helloRequest, new FutureCallback<ReplyPayload>() {
//            @Override
//            public void onSuccess(@NullableDecl ReplyPayload replyPayload) {
//                System.out.printf("requestFuture resp: %s", replyPayload.getMessage());
//                System.out.println("");
//            }
//
//            @Override
//            public void onFailure(Throwable throwable) {
//                System.out.println("requestFuture onFailure");
//            }
//        });
//        List<RequestPayload> listRequestPayload = new ArrayList<>();
//        listRequestPayload.add(helloRequest);
//        grpcClient.requestBiStream(listRequestPayload);
    
 //   }
    
    public void init(String target) {
        managedChannel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        this.requestBlockingStub = RequestGrpc.newBlockingStub(managedChannel);
        this.requestStub = RequestGrpc.newStub(managedChannel);
        this.requestFutureStub = RequestGrpc.newFutureStub(managedChannel);
    }

    public ReplyPayload requestBlocking(RequestPayload requestPayload,Long timeOut) {
        //阻塞API
        RequestGrpc.RequestBlockingStub requestBlockingStub = RequestGrpc.newBlockingStub(managedChannel)
                .withDeadlineAfter(timeOut, TimeUnit.SECONDS);
        ReplyPayload request = requestBlockingStub.request(requestPayload);
        return request;

    }
    
    public ReplyPayload requestBlocking(RequestPayload requestPayload) {
        //阻塞API
        return this.requestBlockingStub.request(requestPayload);
    }
    
    public void requestNonBlocking(RequestPayload requestPayload) {
        //非阻塞API
        this.requestStub.request(requestPayload, new StreamObserver<ReplyPayload>() {
            @Override
            public void onNext(ReplyPayload replyPayload) {
//                System.out.printf("resp: %s", replyPayload.getMessage());
//                System.out.println("");
            }
            
            @Override
            public void onError(Throwable t) {
                System.err.println("error");
            }
            
            @Override
            public void onCompleted() {
                System.out.println("complete");
            }
        });
        
    }
    
    public ListenableFuture<ReplyPayload> requestFuture(RequestPayload requestPayload) {
          return requestFutureStub.request(requestPayload);
         //Futures.addCallback(request, futureCallback, directExecutor());
    }
    
    public void requestBiStream(List<RequestPayload> lequestPayloadList) {
        StreamObserver<RequestPayload> requestObserver = this.requestStub.requestBiStream(new StreamObserver<ReplyPayload>() {
            
            @Override
            public void onNext(ReplyPayload replyPayload) {
//                System.out.printf("BiStream resp: %s", replyPayload.getMessage());
//                System.out.println("");
            }
            
            @Override
            public void onError(Throwable t) {
                System.err.println("error");
            }
            
            @Override
            public void onCompleted() {
                System.out.println("BiStream complete");
            }
        });
        lequestPayloadList.forEach(e -> {
            requestObserver.onNext(e);
        });
        //结束发送请求
        requestObserver.onCompleted();
    }
}
