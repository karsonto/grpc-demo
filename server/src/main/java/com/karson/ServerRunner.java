package com.karson;

import com.karson.service.impl.GrpcServiceImpl;
import io.grpc.*;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ServerRunner {
    Server server;
    
    
    public void serverstart() throws IOException {
        Integer port = 9090;
        server = ServerBuilder.forPort(port)
                //注册服务端实现类
                .addService(new GrpcServiceImpl())
//                .intercept(new ServerInterceptor() {
//                    @Override
//                    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
//                        return null;
//                    }
//                })
                .build();
        server.start();
        System.out.println("gRPC Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down gRPC server since JVM is shutting down");
            ServerRunner.this.shutdown();
            System.err.println("*** server shut down");
        }));
    }
    
    public void shutdown() {
        if (null != server) {
            server.shutdown();
        }
    }
    // block 一直到退出程序
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }
    
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        ServerRunner serverRunner = new ServerRunner();
        serverRunner.serverstart();
        serverRunner.blockUntilShutdown();
    }
}
