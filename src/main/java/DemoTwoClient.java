import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.examples.grpcDemoTwo.CalculateServiceGrpc;
import io.grpc.examples.grpcDemoTwo.CalculateServiceGrpc.CalculateServiceStub;
import io.grpc.examples.grpcDemoTwo.GrpcDemoTwo;
import io.grpc.stub.StreamObserver;

public class DemoTwoClient {
    private static final String DEFAULT_HOST = "localhost";

    private static final int DEFAULT_PORT = 55556;

    private static final int VALUE_NUM = 10;

    private static final int VALUE_UPPER_BOUND = 10;

    private static final Logger log = Logger.getLogger(DemoTwoClient.class.getName());

    private CalculateServiceStub calculateServiceStub;

    public DemoTwoClient(String host,int port){
        this(ManagedChannelBuilder.forAddress(host,port).usePlaintext(true).build());
    }

    public DemoTwoClient(ManagedChannel managedChannel){
        this.calculateServiceStub = CalculateServiceGrpc.newStub(managedChannel);
    }

    public void getResult(List<Integer> nums){

        //判断调用状态。在内部类中被访问，需要加final修饰
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        StreamObserver<GrpcDemoTwo.Result> responseObserver = new StreamObserver<GrpcDemoTwo.Result>() {
            private int cnt = 0;
            public void onNext(GrpcDemoTwo.Result result) {
                log.info("第"+(++cnt)+"次调用得到结果为:"+result);
            }

            public void onError(Throwable throwable) {
                log.info("调用出错:"+throwable.getMessage());
                countDownLatch.countDown();
            }

            public void onCompleted() {
                log.info("调用完成");
                countDownLatch.countDown();
            }
        };

        StreamObserver<GrpcDemoTwo.Value> requestObserver = calculateServiceStub.getResult(responseObserver);
        for(int num: nums){
            GrpcDemoTwo.Value value = GrpcDemoTwo.Value.newBuilder().setValue(num).build();
            requestObserver.onNext(value);

            //判断调用结束状态。如果整个调用已经结束，继续发送数据不会报错，但是会被舍弃
            if(countDownLatch.getCount() == 0){
                return;
            }
        }
        //异步请求，无法确保onNext与onComplete的完成先后顺序
        requestObserver.onCompleted();

        try {
            //如果在规定时间内没有请求完，则让程序停止
            if(!countDownLatch.await(5, TimeUnit.MINUTES)){
                log.info("未在规定时间内完成调用");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    public static void main(String[] args) {

        DemoTwoClient additionClient = new DemoTwoClient(DEFAULT_HOST,DEFAULT_PORT);

        //生成value值
        List<Integer> list = new ArrayList<Integer>();
        Random random = new Random();

        for(int i=0; i<VALUE_NUM; i++){
            //随机数符合 0-VALUE_UPPER_BOUND 均匀分布
            int value = random.nextInt(VALUE_UPPER_BOUND);

            list.add(value);
        }

        System.out.println("*************************从服务器获取的结果***************************");
        System.out.println();

        additionClient.getResult(list);

    }
}
