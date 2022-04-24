package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import java.util.Scanner; 

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
            TFIndexer tfIndexer = new TFIndexer("./db/TFIndex", idManager);
            WeightCalc weightCalc = new WeightCalc("./db/WeightIndex", tfIndexer, forwardIndexer, titleIndexer, bodyIndexer, idManager); 

            Parser p = new Parser(pidIndexer, widIndexer, titleIndexer, bodyIndexer, forwardIndexer, ppIndexer, tfIndexer);
            Crawler c = new Crawler("https://cse.hkust.edu.hk/", p);

            c.crawlLoop();  //Crawl
            weightCalc.processWeight();   //Process all weights  

            idManager.toTextFile("pidPrint.txt", "widPrint.txt");

            // bodyIndexer.printAll();       // UNCOMMENT TO CHECK THE DATABASE
            // titleIndexer.printAll(); 
            // forwardIndexer.printAll();
            // ppIndexer.printAll(); 
            //tfIndexer.printAll(); 
            //weightCalc.printAll(); 
            // pidIndexer.printAll();
            //tfIndexer.toTextFile("tfIndexer.txt");
            // forwardIndexer.toTextFile("forwardIndexer.txt"); 
            // bodyIndexer.toTextFile("bodyIndexer.txt");
            
            Scanner s = new Scanner(System.in); 
            System.out.println("Enter your query: "); 
            String query = s.nextLine(); 

            CosSim cossim = new CosSim("./db/CosSimIndex", idManager, query, weightCalc, forwardIndexer, titleIndexer, bodyIndexer, p);
            cossim.calc();
            //cossim.printAll();

        }

        catch (RocksDBException e) {
            System.out.println(e);
        }
        // IDManager.main(args);
    }
}
