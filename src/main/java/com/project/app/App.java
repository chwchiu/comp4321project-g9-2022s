package com.project.app;
import org.rocksdb.RocksDB;

public class App 
{
    public static void main( String[] args )
    {
        RocksDB.loadLibrary();
        String url = "https://cse.hkust.edu.hk/";
        Crawler crawler = new Crawler(url);
        crawler.crawlLoop();
    }
}
