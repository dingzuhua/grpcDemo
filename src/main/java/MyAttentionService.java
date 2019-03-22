import io.grpc.BindableService;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import io.grpc.examples.MyAttention.*;
import io.grpc.stub.StreamObserver;
public class MyAttentionService {
    private int port = 55557;
    private Server server;

    // 实现 定义一个实现服务接口的类
    private class MyAttentionServiceImpl extends MyAttentionServiceGrpc.MyAttentionServiceImplBase{
        @Override
        public void communication(MyAttentionRequest request, StreamObserver<MyAttentionResponse> responseObserver) {
            System.err.println("service:workId==" + request.getWorkId());
            List<SeniorInfo> lists = new ArrayList<SeniorInfo>();
            for (int i=(request.getPage()-1)*request.getNumber()+1;i<=request.getPage()*request.getNumber();i++){
                SeniorInfo seniorInfo = SeniorInfo.newBuilder().setName("张三"+i).setSeniorId(i).setAge(i+90).setGender(i%2==0?"男":"女").setAddress("上海临汾社区爱照护长者长者照护之家"+i).build();
                lists.add(seniorInfo);
            }
            MyAttentionResponse response = MyAttentionResponse.newBuilder().addAllSeniorInfo(lists).setAllNumber(100).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }

    private void start(int port) throws IOException{
        server = ServerBuilder.forPort(port).addService((BindableService)new MyAttentionService.MyAttentionServiceImpl()).build().start();
        System.err.println("我的关注服务启动，端口： "+port);

        Runtime.getRuntime().addShutdownHook(new Thread(){
            @Override
            public void run() {
                MyAttentionService.this.stop();
                System.err.println("我的关注服务停止");
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
        final MyAttentionService server = new MyAttentionService();
        for (int i=55555;i<55570;i++)
        server.start(i);
        server.blockUntilShutdown();
    }
}
