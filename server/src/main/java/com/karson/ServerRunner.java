package com.karson;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.karson.service.impl.GrpcServiceImpl;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ServerCall;
import io.grpc.ServerCallExecutorSupplier;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerRunner {
    Server server;
    Properties properties;
    static int DEFAULT_PORT = 9999;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("cus thread pool-%d")
            .build();

    public ServerRunner(Properties properties) {
        this.properties = properties;
    }

    public void serverstart() throws IOException, InterruptedException {
        String protStr = properties.getProperty("config-server-port");
        if(null != protStr){
           System.setProperty("config-server-port",protStr);
        }
        Integer port = Integer.getInteger("config-server-port", DEFAULT_PORT);
        server = ServerBuilder.forPort(port)
                //注册服务端实现类
                .addService(new GrpcServiceImpl())
//                .callExecutor(new ServerCallExecutorSupplier() {
//                    @Nullable
//                    @Override
//                    public <ReqT, RespT> Executor getExecutor(ServerCall<ReqT, RespT> serverCall, Metadata metadata) {
//                        return Executors.newSingleThreadExecutor(threadFactory);
//                    }
//                })
//                .intercept(new ServerInterceptor() {
//                    @Override
//                    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
//                        return null;
//                    }
//                })
                .build();
        server.start();
        System.out.println("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("*** shutting down server since JVM is shutting down");
            ServerRunner.this.shutdown();
            System.err.println("*** server shut down");
        }));
        blockUntilShutdown();
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
        ServerRunner serverRunner = new ServerRunner(new Properties());
        serverRunner.serverstart();

    }
}
