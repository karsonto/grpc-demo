package com.karson.grpc;

import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestGrpc;
import com.karson.api.grpc.RequestPayload;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

public class GrpcClient {
    private RequestGrpc.RequestBlockingStub requestBlockingStub;
    private RequestGrpc.RequestStub requestStub;
    
    public static void main(String[] args) throws InterruptedException {
        String target = "localhost:9090";
        //初始化client stub
        GrpcClient grpcClient = new GrpcClient();
        grpcClient.init(target);
        RequestPayload helloRequest = RequestPayload.newBuilder()
                .setMessage("Karson")
                .build();
        ReplyPayload replyPayload = grpcClient.requestBlocking(helloRequest);
        System.out.println(replyPayload.getMessage());
        grpcClient.requestNonBlocking(helloRequest);
        Thread.sleep(1000);
    
    }
    
    private void init(String target) {
        ManagedChannel managedChannel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        this.requestBlockingStub = RequestGrpc.newBlockingStub(managedChannel);
        this.requestStub = RequestGrpc.newStub(managedChannel);
    }
    
    public ReplyPayload requestBlocking(RequestPayload requestPayload){
        //阻塞API
       return this.requestBlockingStub.request(requestPayload);
    }
    public void requestNonBlocking(RequestPayload requestPayload){
        //非阻塞API
        this.requestStub.request(requestPayload, new StreamObserver<ReplyPayload>() {
            @Override
            public void onNext(ReplyPayload replyPayload) {
                System.out.printf("resp: %s", replyPayload.getMessage());
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
}
