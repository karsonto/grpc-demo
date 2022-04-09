package com.karson.api.grpc;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.44.0)",
    comments = "Source: grpc_service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class RequestGrpc {

  private RequestGrpc() {}

  public static final String SERVICE_NAME = "Request";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<RequestPayload,
      ReplyPayload> getRequestMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "request",
      requestType = RequestPayload.class,
      responseType = ReplyPayload.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<RequestPayload,
      ReplyPayload> getRequestMethod() {
    io.grpc.MethodDescriptor<RequestPayload, ReplyPayload> getRequestMethod;
    if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
      synchronized (RequestGrpc.class) {
        if ((getRequestMethod = RequestGrpc.getRequestMethod) == null) {
          RequestGrpc.getRequestMethod = getRequestMethod =
              io.grpc.MethodDescriptor.<RequestPayload, ReplyPayload>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "request"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  RequestPayload.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  ReplyPayload.getDefaultInstance()))
              .setSchemaDescriptor(new RequestMethodDescriptorSupplier("request"))
              .build();
        }
      }
    }
    return getRequestMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static RequestStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestStub>() {
        @Override
        public RequestStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestStub(channel, callOptions);
        }
      };
    return RequestStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static RequestBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestBlockingStub>() {
        @Override
        public RequestBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestBlockingStub(channel, callOptions);
        }
      };
    return RequestBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static RequestFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<RequestFutureStub>() {
        @Override
        public RequestFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new RequestFutureStub(channel, callOptions);
        }
      };
    return RequestFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class RequestImplBase implements io.grpc.BindableService {

    /**
     */
    public void request(RequestPayload request,
                        io.grpc.stub.StreamObserver<ReplyPayload> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRequestMethod(), responseObserver);
    }

    @Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getRequestMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                RequestPayload,
                ReplyPayload>(
                  this, METHODID_REQUEST)))
          .build();
    }
  }

  /**
   */
  public static final class RequestStub extends io.grpc.stub.AbstractAsyncStub<RequestStub> {
    private RequestStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequestStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestStub(channel, callOptions);
    }

    /**
     */
    public void request(RequestPayload request,
                        io.grpc.stub.StreamObserver<ReplyPayload> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class RequestBlockingStub extends io.grpc.stub.AbstractBlockingStub<RequestBlockingStub> {
    private RequestBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequestBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestBlockingStub(channel, callOptions);
    }

    /**
     */
    public ReplyPayload request(RequestPayload request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRequestMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class RequestFutureStub extends io.grpc.stub.AbstractFutureStub<RequestFutureStub> {
    private RequestFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected RequestFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new RequestFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<ReplyPayload> request(
        RequestPayload request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRequestMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_REQUEST = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final RequestImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(RequestImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_REQUEST:
          serviceImpl.request((RequestPayload) request,
              (io.grpc.stub.StreamObserver<ReplyPayload>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @Override
    @SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    RequestBaseDescriptorSupplier() {}

    @Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return GrpcService.getDescriptor();
    }

    @Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("Request");
    }
  }

  private static final class RequestFileDescriptorSupplier
      extends RequestBaseDescriptorSupplier {
    RequestFileDescriptorSupplier() {}
  }

  private static final class RequestMethodDescriptorSupplier
      extends RequestBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    RequestMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (RequestGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new RequestFileDescriptorSupplier())
              .addMethod(getRequestMethod())
              .build();
        }
      }
    }
    return result;
  }
}
