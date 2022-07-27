import com.karson.api.remote.ConfigService;
import com.karson.config.ConfigFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ConfigService configService = ConfigFactory.createConfigService(new Properties());

        countDownLatch.await();

    }

}
