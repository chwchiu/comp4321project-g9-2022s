package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class ForwardIndexer extends Indexer {
    private IDManager idManager;

    ForwardIndexer(String dbPath, IDManager idManager) throws RocksDBException
    {
        super(dbPath);
        this.idManager = idManager;
    }

    // TODO: Forward Index addEntry -> massage Data


    public void addEntry(String url, String word) throws RocksDBException
    {
       String wordID = idManager.getWordId(word);
       String docID = idManager.getUrlId(url); 
       byte[] content = db.get(docID.getBytes());

       if (content == null){
           content = (wordID + ",").getBytes();
       }
        else {
            content = (new String(content) + wordID + ",").getBytes();
        }

        db.put(docID.getBytes(), content);
    }

}
