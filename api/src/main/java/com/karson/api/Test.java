package com.karson.api;

import com.karson.api.common.Multimap;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

public class Test {

    public static void main(String[] args) {
        Multimap<Set<String>, String> asyncContextContainer = Multimap.createSetMultimap();
        TreeSet<String> t1 =  new TreeSet<>();
        Set<String> t2 =  new HashSet<>();
        t1.add("a");
        t1.add("b");
        t1.add("c");
        //t1.add("2");
        t2.add("b");
        t2.add("a");
        t2.add("c");
        asyncContextContainer.put(t1,"a");
        System.out.println(asyncContextContainer.containsKey(t2));
    }

}
