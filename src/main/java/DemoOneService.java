import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.examples.grpcDemoOne.DemoOneRequest;
import io.grpc.examples.grpcDemoOne.DemoOneResponse;
import io.grpc.examples.grpcDemoOne.DemoOneServiceGrpc;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class DemoOneService {
    private int port = 55555;
    private Server server;

    // 实现 定义一个实现服务接口的类
    private class DemoOneServiceImpl extends DemoOneServiceGrpc.DemoOneServiceImplBase{
        @Override
        public void communication(DemoOneRequest request, StreamObserver<DemoOneResponse> responseObserver) {
            System.err.println("service:" + request.getMessage());
            DemoOneResponse response = DemoOneResponse.newBuilder().setCode(200).setMessage("接收到："+ request.getMessage()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private void start() throws IOException{
        server = ServerBuilder.forPort(port).addService((BindableService)new DemoOneServiceImpl()).build().start();
        System.err.println("一号服务启动，端口： "+port);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                DemoOneService.this.stop();
                System.err.println("一号服务停止");
            }
        });
    }

    private void stop(){
        if(server!=null){
            server.shutdown();
        }
    }

    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final DemoOneService server = new DemoOneService();
        server.start();
        server.blockUntilShutdown();
    }
}
