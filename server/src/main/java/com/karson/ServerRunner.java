package com.karson;

import com.karson.service.impl.GrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.Properties;

public class ServerRunner {
    Server server;
    Properties properties;
    static int DEFAULT_PORT = 9999;

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
