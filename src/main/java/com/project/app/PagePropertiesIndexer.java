package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class PagePropertiesIndexer extends Indexer {
    private RocksDB db;

    PagePropertiesIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
    }

    public void addEntry(String key, String val) throws RocksDBException {
        // TODO: addEntry
    }
}
