package com.project.app;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import org.rocksdb.RocksIterator;

public class Retrieval {
    CosSim cs; 

    /**
     * Constructor for retrieval class
     * @param cs the database of the cosine similarity scores
     */
    Retrieval(CosSim cs) {
        this.cs = cs; 
    }
    
    public class Pair{
        String key;
        String value; 
        
        /**
         * Helper class for sorting 
         * @param key the key of the pair
         * @param value the value of the pair
         */
        Pair(String key, String value) {
            this.key = key; 
            this.value = value; 
        }
        
        /**
         * Getter for key 
         * @return the key of the pair
         */
        public String getKey() {return key;}
        /**
         * Getter for pair
         * @return the value of the pair 
         */
        public String getValue() {return value;}
    }

    /**
     * Functiont that retrieves the top50 ranked pages according to cosine similarity
     * @return returns the top 50 pages 
     */
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
