package com.project.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.rocksdb.RocksIterator;

public class Retrieval {
    CosSim cs; 
    Retrieval(CosSim cs) {
        this.cs = cs; 
    }
    
    public class Pair{
        String key;
        String value; 

        Pair(String key, String value) {
            this.key = key; 
            this.value = value; 
        }

        public String getKey() {return key;}
        public String getValue() {return value;}
    }

    public HashMap<Integer, String> top50() {
        HashMap<Integer, String> result = new HashMap<Integer, String>(); 
        ArrayList<Pair> temp = new ArrayList<Pair>(); 
        RocksIterator iter = cs.db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {   //Store all values into arraylist
            System.out.println("key: " + new String(iter.key()) + " val: " + new String(iter.value()));
            temp.add(new Pair(new String(iter.key()), new String(iter.value()))); 
        }

        Collections.sort(temp, Comparator.comparing(p -> p.getValue()));   //Sort by descending 
        Collections.reverse(temp); 

        for (Integer i = 0; i < 50; i++) {
            result.put(i+1, temp.get(i).getKey()); 
        }
        return result; 
    }
}
