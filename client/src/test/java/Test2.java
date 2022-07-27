import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.checkerframework.checker.nullness.compatqual.NullableDecl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static com.google.common.util.concurrent.MoreExecutors.directExecutor;

public class Test2 {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
          ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("longPolling-timeout-checker-%d")
                .build();

          ScheduledExecutorService timeoutChecker = new ScheduledThreadPoolExecutor(1, threadFactory);
        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
        ListeningExecutorService executorService2= MoreExecutors.newDirectExecutorService();
        System.out.println(System.currentTimeMillis());
        ListenableFuture<Integer> submit = executorService.submit(() -> {
            Thread.sleep(10000L);
            return 1;
        });
        ListenableFuture<Integer> integerListenableFuture = Futures.withTimeout(submit, 10L, TimeUnit.SECONDS,
                timeoutChecker);
        Futures.addCallback(integerListenableFuture, new FutureCallback() {
            @Override
            public void onSuccess(@NullableDecl Object o) {
                  System.out.println(o);
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.println(System.currentTimeMillis());
                System.out.println(throwable);
            }
        },executorService2);
        System.out.println("2");
        countDownLatch.await();
    }

}
