import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.grpcDemoOne.DemoOneRequest;
import io.grpc.examples.grpcDemoOne.DemoOneResponse;
import io.grpc.examples.grpcDemoOne.DemoOneServiceGrpc;
import io.grpc.examples.grpcDemoOne.DemoOneServiceGrpc.DemoOneServiceBlockingStub;

import java.util.concurrent.TimeUnit;

public class DemoOneClient {
    private final ManagedChannel managedChannel;
    private final DemoOneServiceBlockingStub blockingStub;

    public DemoOneClient(String host,int port){
        managedChannel = ManagedChannelBuilder.forAddress(host,port).usePlaintext(true).build();
        blockingStub = DemoOneServiceGrpc.newBlockingStub(managedChannel);
    }

    public void shutdown() throws InterruptedException {
        managedChannel.shutdown().awaitTermination(5,TimeUnit.SECONDS);
    }

    public void communicationTest(String message){
        DemoOneRequest request = DemoOneRequest.newBuilder().setMessage(message).build();
        DemoOneResponse response = blockingStub.communication(request);
        System.out.println("编号:"+response.getCode()+"==消息:"+response.getMessage());
    }

    public static void main(String[] args) throws InterruptedException{
        DemoOneClient client = new DemoOneClient("127.0.0.1",55555);
        for (int i=0;i<2;i++){
            client.communicationTest("客户端发来的消息"+i);
        }
    }
}
