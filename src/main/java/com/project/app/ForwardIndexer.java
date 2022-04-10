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
    public void addEntry(String url, String body, String title) throws RocksDBException
    {
        db.put(idManager.getUrlId(url).getBytes(), body.getBytes());
    }
}
