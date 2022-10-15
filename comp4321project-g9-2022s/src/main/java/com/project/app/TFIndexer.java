package com.project.app; 

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException; 
import java.util.Map;
import java.util.HashMap; 

public class TFIndexer extends Indexer {
    private IDManager idManager;

    /**
     * Constructor for tfindexer
     * @param dbPath the path for the indexer 
     * @param idManager the manager for ids
     * @throws RocksDBException rocks db exception 
     */
    TFIndexer(String dbPath, IDManager idManager) throws RocksDBException {
        super(dbPath); 
        this.idManager = idManager; 
    }

    /** Adds url, term frequency list key pair into tf database
     * @param url the document 
     * @param wordFreq a hashmap of word : tf key pairs
     * @throws RocksDBException rocks db exception 
     */
    public void addEntry(String url, HashMap<String, Integer> wordFreq) throws RocksDBException{
        String formattedWordFreq = ""; 
        for (Map.Entry<String, Integer> set : wordFreq.entrySet()) {
            String wordID = idManager.getWordId(set.getKey()); 
            formattedWordFreq = formattedWordFreq + "," + wordID + ":" + set.getValue();
        }
        
        byte[] content = formattedWordFreq.getBytes();
        String docID = idManager.getUrlId(url); 
        db.put(docID.getBytes(), content); 
    }
}