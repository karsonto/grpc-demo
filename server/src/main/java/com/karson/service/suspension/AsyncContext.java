package com.karson.service.suspension;

import com.karson.api.grpc.ReplyPayload;
import com.karson.api.grpc.RequestPayload;
import io.grpc.stub.StreamObserver;

public class AsyncContext {

    private RequestPayload request;
    private StreamObserver<ReplyPayload> responseObserver;

    private volatile boolean isTimeOut = true;

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
