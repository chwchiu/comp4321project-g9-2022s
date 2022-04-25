package com.project.app;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class IDIndexer extends Indexer {
    private Integer counter = 0;

    IDIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
        
        RocksIterator iter = db.newIterator();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) counter++;
    }

    public boolean addEntry(String word) throws RocksDBException
    {
        byte[] content = db.get(word.getBytes());
        if (content == null){
            db.put(word.getBytes(), counter.toString().getBytes());
            counter++;
            return true;
        }
        else return false;
    }

    public String getKeyfromVal(String val) throws RocksDBException
    {
        RocksIterator iter = db.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            if(new String(iter.value()) == val) return new String(iter.key());
        }
        return "";
    }
}
