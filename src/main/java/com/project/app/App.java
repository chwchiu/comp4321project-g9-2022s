package com.project.app;

import java.util.List;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

public class App 
{
    public static void main( String[] args )
    {
        // RocksDB.loadLibrary();
        // String url = "https://cse.hkust.edu.hk/";
        // Crawler crawler = new Crawler(url);
        // crawler.crawlLoop();
        
        // RocksDB.loadLibrary();
        //setup all dbs
        //crawl
        
        //ask for user input
        //send input to retrieval
        //Result[] results = search(input);
        //display results

        RocksDB.loadLibrary();
        try{
            //setup all dbs
            Indexer indexer = new Indexer("./db/indexer");
            InvertedIndexer indexer2 = new InvertedIndexer("./db/indexer2");
            Parser p = new Parser(indexer, indexer2); 
            Crawler c = new Crawler("https://cse.hkust.edu.hk/", p);
            c.crawlLoop();  
        }
        catch (RocksDBException e) {
            System.out.println(e);
        }
    }
}
