import com.karson.api.listener.Listener;
import com.karson.api.remote.ConfigService;
import com.karson.config.ConfigFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(2);
        new Thread(()->{
            ConfigService configService = ConfigFactory.createConfigService(new Properties());
            configService.addListener("karson",new Listener(){

                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("received : " + configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

            for(int i = 0; i<1000;i++){
                configService.publishConfig("karson", "publish karson" + i);
                //Thread.sleep(20L);
            }
        }).start();
        new Thread(()->{
            ConfigService configService = ConfigFactory.createConfigService(new Properties());
            configService.addListener("aa",new Listener(){

                @Override
                public void receiveConfigInfo(String configInfo) {
                    System.out.println("received : " + configInfo);
                }

                @Override
                public Executor getExecutor() {
                    return null;
                }
            });

            for(int i = 0; i<1000;i++){
                configService.publishConfig("aa", "publish aa " + i);
                //Thread.sleep(20L);
            }
        }).start();

        countDownLatch.await();

    }

}
