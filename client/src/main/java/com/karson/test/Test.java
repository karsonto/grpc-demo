package com.karson.test;

import com.karson.config.ConfigFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class Test {

    public static void main(String[] args) throws InterruptedException {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ConfigFactory.createConfigService(new Properties());
        countDownLatch.await();

    }

}
