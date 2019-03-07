import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.logging.Logger;
import io.grpc.examples.grpcDemoTwo.CalculateServiceGrpc.CalculateServiceImplBase;
import io.grpc.examples.grpcDemoTwo.*;
import io.grpc.stub.StreamObserver;

public class DemoTwoService {
    private static final Logger logger = Logger.getLogger(DemoTwoService.class.getName());
    private static final int DEFAULT_PORT = 55556;
    private int port;//服务端口号
    private Server server;

    public DemoTwoService(int port){
        this(port,ServerBuilder.forPort(port));
    }

    public DemoTwoService(int port,ServerBuilder<?> serverBuilder){

        this.port = port;
        //构造服务器，添加我们实际的服务
        server = serverBuilder.addService(new CalculateServiceImpl()).build();
    }

    private void start() throws IOException {
        server.start();
        logger.info("二号服务启动，端口：" + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                DemoTwoService.this.stop();
                logger.info("二号服务停止" );
            }
        });

    }

    private void stop() {

        if(server != null)
            server.shutdown();

    }

    //阻塞到应用停止
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    public static void main(String[] args) {

        DemoTwoService addtionServer;

        if(args.length > 0){
            addtionServer = new DemoTwoService(Integer.parseInt(args[0]));
        }else{
            addtionServer = new DemoTwoService(DEFAULT_PORT);
        }

        try {
            addtionServer.start();
            addtionServer.blockUntilShutdown();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    // 实现 定义一个实现服务接口的类
    private class CalculateServiceImpl extends CalculateServiceImplBase{

        private Logger log = Logger.getLogger(CalculateServiceImpl.class.getName());

        @Override
        public StreamObserver<GrpcDemoTwo.Value> getResult(final StreamObserver<GrpcDemoTwo.Result> responseObserver) {
            return new StreamObserver<GrpcDemoTwo.Value>() {
                private int sum = 0;
                private int cnt = 0;
                private double avg;

                public void onNext(GrpcDemoTwo.Value value) {
                    log.info("接收到消息为:"+value.getValue());
                    sum += value.getValue();
                    cnt++;
                    avg = 1.0*sum/cnt;
                    //返回当前统计结果
                    GrpcDemoTwo.Result response = GrpcDemoTwo.Result.newBuilder().setSum(sum).setCnt(cnt).setAvg(avg).build();
                    log.info("返回消息为:"+response);
                    responseObserver.onNext(response);
                }

                public void onError(Throwable throwable) {
                    log.info("调用出错:"+throwable.getMessage());
                }

                public void onCompleted() {
                    responseObserver.onCompleted();
                }
            };
        }
    }
}
