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
    public void addEntry(String keyword, String body) throws RocksDBException
    {
        // db.put(idManager.getUrlId(url).getBytes(), data.getBytes()); <- might be helpful
        
        // Add a "docX Y" entry for the key "word" into hashtable
        // byte[] content = db.get(keyword.getBytes());
        // if (content == null) {
        //     content = ("doc" + x + " " + y).getBytes();
        // } else {
        //     content = (new String(content) + " doc" + x + " " + y).getBytes();
        // }
        // db.put(keyword.getBytes(), content);
    }
}