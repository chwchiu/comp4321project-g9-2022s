//IMPORTANT READ!
//MAKE SURE TO REDO ADDENTRY FOR PROJECT!
package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class InvertedIndexer extends Indexer
{
    private IDManager idManager;

    InvertedIndexer(String dbPath, IDManager idManager) throws RocksDBException
    {
        super(dbPath);
        this.idManager = idManager;
    }

    //TODO: update this old addEntry
    public void addEntry(String url, String word, String positions) throws RocksDBException
    {
        // db.put(idManager.getUrlId(keyword).getBytes(), body.getBytes()); //ASK NATE ABOUT THIS
        
        // Add a "docX Y" entry for the key "word" into hashtable
        String docID = idManager.getUrlId(url); 
        String wordID = idManager.getWordId(word); 
        byte[] content = db.get(wordID.getBytes());
        if (content == null) {
            content = ("doc" + docID + "#" + positions).getBytes();
        } else {
            content = (new String(content) + " doc" + docID + "#" + positions).getBytes();
        }
        db.put(wordID.getBytes(), content);
    }
}