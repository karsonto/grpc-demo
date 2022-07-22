package com.karson.api.common;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @ClassName Multimap
 * @Description TODO
 * @Author Karson
 * @Date 2022-07-21 13:39
 * @Version 1.0
 **/
public class Multimap<T, V> implements Closeable {

    private static int initialCapacity = 16;

    private static Map<Object, Multimap<?, ?>> multimapManager = new ConcurrentHashMap<>(initialCapacity);

   // private static Map<Object, Class<? extends Collection>> classMap = new ConcurrentHashMap<>(initialCapacity);

    private static ThreadLocal<Map<Object, Multimap<?, ?>>> threadLocalMultimapManager = ThreadLocal.withInitial(
            () -> new ConcurrentHashMap<>(initialCapacity));

    private Map<T, Collection<V>> containerMap;

    private Map<T, ReadWriteLock> lockMap;


    private static final Class<? extends Collection> DEFAULTLISTCLASS = ArrayList.class;

    private Class<? extends Collection> CUSLISTCLASS = DEFAULTLISTCLASS;

    private static final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    private String putIfNull = Integer.toString(threadLocalRandom.nextInt(Integer.MAX_VALUE));



    public static final class Managed {

        public static void removeThreadLocalMultimap(){
            threadLocalMultimapManager.remove();
        }

        public static <T, V> Multimap<T, V> createThreadLocalMultimap(Object key) {
            return createThreadLocalMultimap(key,DEFAULTLISTCLASS);
        }
        public static <T, V> Multimap<T, V> createThreadLocalMultimap(Object key, Class<? extends Collection> listClass) {
            Multimap<T, V> objectObjectMultimap = (Multimap<T, V>) threadLocalMultimapManager.get().computeIfAbsent(key, (k) -> create(listClass));
            //   classMap.putIfAbsent(key,listClass);
            return objectObjectMultimap;
        }
        public static <T, V> Multimap<T, V> getThreadLocalMultimap(Object key) {
            return (Multimap<T, V>) threadLocalMultimapManager.get().get(key);
        }


        public static <T, V> Multimap<T, V> createGlobalMultimap(Object key) {
            return createGlobalMultimap(key,DEFAULTLISTCLASS);
        }

        public static <T, V> Multimap<T, V> createGlobalMultimap(Object key, Class<? extends Collection> listClass) {
            Multimap<T, V> objectMultimap = (Multimap<T, V>) multimapManager.computeIfAbsent(key, (k) -> create(listClass));
         //   classMap.putIfAbsent(key,listClass);
            return objectMultimap;
        }

        public static <T, V> Multimap<T, V> getGlobalMultimap(Object key) {
            return (Multimap<T, V>) multimapManager.get(key);
        }

//        public static Class<? extends Collection> getGlobalMultimapListClass(Object key) {
//            return classMap.get(key);
//        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
         //   classMap.clear();
         threadLocalMultimapManager = null;
            for (Map.Entry<Object, Multimap<?, ?>> entry : multimapManager.entrySet()) {
                try {
                    Multimap<?, ?> value = entry.getValue();
                    value.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public Multimap() {
        this(initialCapacity);
    }

    public Multimap(int size) {
        containerMap = new ConcurrentHashMap<>(size);
        lockMap = new ConcurrentHashMap<>(size);
    }

    public Multimap(Class<? extends Collection> clazz) {
        this();
        this.CUSLISTCLASS = clazz;
    }

    public Set<T> keySet(){
        return containerMap.keySet();
    }

    public void clear(T key){
        if (null == key) {
            key = (T) putIfNull;
        }
        ReadWriteLock readWriteLock = lockMap.get(key);
        if (null == readWriteLock) {
            return;
        }
        Lock lock = readWriteLock.writeLock();
        try{
            lock.lock();
            Collection<V> vs = containerMap.get(key);
            vs.clear();
        }finally {
            lock.unlock();
        }
    }

    public void clearAndPut(T key ,V value){
        clear(key);
        put(key,value);

    }


    public void remove(T key ,V value){
        if (null == key) {
            key = (T) putIfNull;
        }
        ReadWriteLock readWriteLock = lockMap.get(key);
        if (null == readWriteLock) {
            return;
        }
        Lock lock = readWriteLock.writeLock();
        try {
            lock.lock();
            Collection<V> vs = containerMap.get(key);
            vs.remove(value);
        } finally {
            lock.unlock();
        }


    }


    public void put(T key, V value) {
        if (null == key) {
            key = (T) putIfNull;
        }
        ReadWriteLock lock = lockMap.computeIfAbsent(key, (k) -> new ReentrantReadWriteLock());
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            Collection<V> collection = containerMap.computeIfAbsent(key, (k) -> {
                    try {
                        //                        Class clz = this.getClass();
                        //                        Type type = clz.getGenericSuperclass();
                        //                        ParameterizedType pt = (ParameterizedType)type;
                        //                        Class modelClass = (Class)pt.getActualTypeArguments()[1];
                        return this.CUSLISTCLASS.newInstance();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                  return new ArrayList<>();
            });
            collection.add(value);
        } finally {
            writeLock.unlock();
        }
    }

    public boolean containsKey(T key){
        return containerMap.containsKey(key);
    }


    public void putIfAbsent(T key, V value) {
          if(!containsKey(key)){
              put(key,value);
          }
    }

    public Optional<Collection<V>> get(T key) {
        if (null == key) {
            key = (T) putIfNull;
        }
        ReadWriteLock readWriteLock = lockMap.get(key);
        if (null == readWriteLock) {
            return Optional.empty();
        }
        Lock lock = readWriteLock.readLock();
        try {
            lock.lock();
            return Optional.of(containerMap.get(key));
        } finally {
            lock.unlock();
        }
    }

    public static <T, V> Multimap<T, V> create() {
        return new Multimap<T, V>();
    }

    public static <T, V> Multimap<T, V> create(int size) {
        return new Multimap<T, V>(size);
    }

    public static <T, V> Multimap<T, V> create(Class<? extends Collection> listClass) {
        return new Multimap<T, V>(listClass);
    }
    public static <T, V> Multimap<T, V> createSetMultimap() {
        return new Multimap<T, V>(HashSet.class);
    }

    @Override
    public void close() throws IOException {
        containerMap.clear();
        lockMap.clear();
    }
}
