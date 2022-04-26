package com.project.app; 

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException; 
import java.util.Map;
import java.util.Vector;
import java.util.HashMap; 

public class ChildIndexer extends Indexer {
    private IDManager idManager;

    /**
     * Constructor for tfindexer
     * @param dbPath the path for the indexer 
     * @param idManager the manager for ids
     * @throws RocksDBException rocks db exception 
     */
    ChildIndexer(String dbPath, IDManager idManager) throws RocksDBException {
        super(dbPath); 
        this.idManager = idManager; 
    }

    /** Adds url, term frequency list key pair into tf database
     * @param url the document 
     * @param wordFreq a hashmap of word : tf key pairs
     * @throws RocksDBException rocks db exception 
     */
    public void addEntry(Vector<String> children, String url) throws RocksDBException{
        String formattedChildren = ""; 
        for (String child : children) {
            formattedChildren = formattedChildren + " " + child; 
        }
        byte[] content = formattedChildren.trim().getBytes();

        String parent = idManager.getUrlId(url); 
        db.put(parent.getBytes(), content); 
    }
}