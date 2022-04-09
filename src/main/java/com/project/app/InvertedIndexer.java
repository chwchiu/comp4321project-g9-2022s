//IMPORTANT READ!
//MAKE SURE TO REDO ADDENTRY FOR PROJECT!
package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class InvertedIndexer extends Indexer
{
    private RocksDB db;

    InvertedIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
    }

    public void addEntry(String keyword, int x, int y) throws RocksDBException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        byte[] content = db.get(keyword.getBytes());
        if (content == null) {
            content = ("doc" + x + " " + y).getBytes();
        } else {
            content = (new String(content) + " doc" + x + " " + y).getBytes();
        }
        db.put(keyword.getBytes(), content);
    }
}