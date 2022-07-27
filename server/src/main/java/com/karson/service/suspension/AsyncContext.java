package com.karson.service.suspension;

import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestPayload;
import io.grpc.ServerCall;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

public class AsyncContext {

    private RequestPayload request;

    private StreamObserver<ReplyPayload> responseObserver;

    private ReentrantLock lock = new ReentrantLock();

    private volatile boolean isTimeOut = true;

    private volatile boolean responsed = false;


    public void execByLock(Consumer<AsyncContext> consumer){
        lock.lock();
        try {
            consumer.accept(this);

        }catch (Exception e){
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }


    public boolean isTimeOut() {
        return isTimeOut;
    }

    public void setTimeOut(boolean timeOut) {
        isTimeOut = timeOut;
    }

    public AsyncContext(RequestPayload request, StreamObserver<ReplyPayload> responseObserver) {
        this.request = request;
        this.responseObserver = responseObserver;
    }

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    public StreamObserver<ReplyPayload> getResponseObserver() {
        return responseObserver;
    }

    public void setResponseObserver(StreamObserver<ReplyPayload> responseObserver) {
        this.responseObserver = responseObserver;
    }
}
