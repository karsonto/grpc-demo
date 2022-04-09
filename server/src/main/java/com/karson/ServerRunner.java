package com.karson;

import com.karson.service.impl.GrpcServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ServerRunner {
    public static void main(String[] args) throws IOException, InterruptedException {
        Integer port = 9090;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Server server = ServerBuilder.forPort(port)
                //注册服务端实现类
                .addService(new GrpcServiceImpl())
                .build()
                .start();
        System.out.println("gRPC Server started, listening on " + port);
        countDownLatch.await();
    }
}
