package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class PagePropertiesIndexer extends Indexer {
    private IDManager idManager;

    PagePropertiesIndexer(String dbPath, IDManager idManager) throws RocksDBException
    {
        super(dbPath);
        this.idManager = idManager;
    }


    // TODO: addEntry - might need to pass different params
    public void addEntry(String url, String body, String title) throws RocksDBException {
        db.put(idManager.getUrlId(url).getBytes(), body.getBytes());
    }
}
