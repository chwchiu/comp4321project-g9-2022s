package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class ForwardIndexer extends Indexer {
    private RocksDB db;

    ForwardIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
    }

    public void addEntry(String url, String data) throws RocksDBException {
        // TODO: Forward Index addEntry
    }
}
