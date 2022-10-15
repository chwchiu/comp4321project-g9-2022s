package com.project.app; 

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException; 
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.HashMap;
import java.util.HashSet; 

public class ParentIndexer extends Indexer {
    private IDManager idManager;

    /**
     * Constructor for tfindexer
     * @param dbPath the path for the indexer 
     * @param idManager the manager for ids
     * @throws RocksDBException rocks db exception 
     */
    ParentIndexer(String dbPath, IDManager idManager) throws RocksDBException {
        super(dbPath); 
        this.idManager = idManager; 
    }

    /** Adds url, term frequency list key pair into tf database
     * @param links list of links to add the parent of 
     * @param parent the parent of all these links
     * @throws RocksDBException rocks db exception 
     */
    public void addEntry(Vector<String> links, String parent) throws RocksDBException{
        Set<String> set = new HashSet<String>();
        set.addAll(links);
        Vector<String> temp = new Vector<>(); 
        temp.addAll(set); 
        String parentID = idManager.getUrlId(parent); 
        byte [] content; 
        for (String link : temp) {
            String docID = idManager.getUrlId(link); 
            content = db.get(docID.getBytes());
            if (content == null) {
                content = parentID.getBytes();
            } else {
                content = (new String(content) + " " + parentID).getBytes();
            }
            db.put(docID.getBytes(), content); 
        }
    }
}