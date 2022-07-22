//package com.karson.service.suspension;
//
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.locks.Condition;
//import java.util.concurrent.locks.ReentrantLock;
//
//public class GuardedObject {
//
//    private AsyncContext asyncContext;
//
//    final ReentrantLock lock = new ReentrantLock();
//
//    final Condition done = lock.newCondition();
//
//    final int timeout = 1;
//
//    public GuardedObject(AsyncContext asyncContext) {
//        this.asyncContext = asyncContext;
//    }
//
//    public AsyncContext getAsyncContext() {
//        lock.lock();
//        try {
//            //管程通用写法
//            while (!asyncContext.isResponse()) {
//                done.await(timeout, TimeUnit.SECONDS);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        } finally {
//            lock.unlock();
//        }
//        //返回受保护的对象
//        return asyncContext;
//    }
//    public boolean onChanged() {
//        lock.lock();
//        try {
//            this.asyncContext.setResponse(true);
//            done.signalAll();
//            return true;
//        } finally {
//            lock.unlock();
//        }
//    }
//}
//
////
////    T get(Predicate<T> p) {
////        lock.lock();
////        try {
////            //管程通用写法
////            while (!p.test(object)) {
////                done.await(timeout, TimeUnit.SECONDS);
////            }
////        } catch (Exception e) {
////            e.printStackTrace();
////        } finally {
////            lock.unlock();
////        }
////        //返回受保护的对象
////        return object;
////    }
////
////
//
////}
