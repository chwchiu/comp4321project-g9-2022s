package com.project.app;
import org.rocksdb.RocksDB;

public class App 
{
    public static void main( String[] args )
    {
        // RocksDB.loadLibrary();
        // String url = "https://cse.hkust.edu.hk/";
        // Crawler crawler = new Crawler(url);
        // crawler.crawlLoop();

        // StopStem.main(args);
        StopStem stopStem = new StopStem("stopwords.txt");
        String input = "This is a test sentence, with a comma. This is test sentence 2.";
        String output = stopStem.ss(input);
        System.out.println(output);
    }
}
