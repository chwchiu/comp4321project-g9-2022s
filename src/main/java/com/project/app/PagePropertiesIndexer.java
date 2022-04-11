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


    /**
     * Adds entries into the page properties db
     * @param url the page url
     * @param lastModified the last modified date of the url
     * @param size the size of the page 
     */
    public void addEntry(String url, String lastModified, String size) throws RocksDBException {
        byte[] content = (new String("Last Modified: " + lastModified + " Size of Doc: " + size)).getBytes();
        String docID = idManager.getUrlId(url);
        if(docID == "") System.out.println("Failed url: " + url);
        db.put(docID.getBytes(), content);
    }
}
