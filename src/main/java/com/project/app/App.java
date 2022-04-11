package com.project.app;

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
        // setup all dbs
        // crawl
        
        // ask for user input
        // send input to retrieval
        // Result[] results = search(input);
        // display results

        RocksDB.loadLibrary();
        try{
            //setup all dbs
            IDIndexer pidIndexer = new IDIndexer("./db/PageIDIndex");
            IDIndexer widIndexer = new IDIndexer("./db/WordIDIndex");
            IDManager idManager = new IDManager(pidIndexer, widIndexer);

            InvertedIndexer bodyIndexer = new InvertedIndexer("./db/BodyIndex", idManager);
            InvertedIndexer titleIndexer = new InvertedIndexer("./db/TitleIndex", idManager);
            ForwardIndexer forwardIndexer = new ForwardIndexer("./db/ForwardIndex", idManager);
            PagePropertiesIndexer ppIndexer = new PagePropertiesIndexer("./db/PagePropertiesIndex", idManager);

            Parser p = new Parser(pidIndexer, widIndexer, bodyIndexer, titleIndexer, forwardIndexer, ppIndexer);
            Crawler c = new Crawler("https://cse.hkust.edu.hk/", p);
            // c.crawlLoop();

            // ppIndexer.printAll();
            // bodyIndexer.printAll(); 
            // forwardIndexer.printAll();
            titleIndexer.printAll(); 
        }
        catch (RocksDBException e) {
            System.out.println(e);
        }
        // IDIndexer.main(args);
    }
}
