package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.util.HashMap;
import java.util.Scanner; 
import java.util.Vector; 

public class App 
{
    public static void main( String[] args )
    {
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
            ChildIndexer ci = new ChildIndexer("./db/ChildIndex", idManager);
            ParentIndexer pi = new ParentIndexer("./db/ParentIndex", idManager);
            Parser p = new Parser(pidIndexer, widIndexer, titleIndexer, bodyIndexer, forwardIndexer, ppIndexer, tfIndexer, pi, ci);
            Crawler c = new Crawler("https://cse.hkust.edu.hk/", p);

            c.crawlLoop();  //Crawl
            weightCalc.processWeight();   //Process all weights
            // idManager.toTextFile("pidPrint.txt", "widPrint.txt");

            // pidIndexer.addEntry("testing");
            // widIndexer.addEntry("word");
            // bodyIndexer.addEntry("testing", "word", "15");
            // bodyIndexer.addEntry("justAnotherTest", "word", "10"); 
            // System.out.println(bodyIndexer.getByKey("testing"));


            // bodyIndexer.printAll();       // UNCOMMENT TO CHECK THE DATABASE
            // titleIndexer.printAll(); 
            // forwardIndexer.printAll();
            // ppIndexer.printAll(); 
            // tfIndexer.printAll(); 
            // weightCalc.printAll();
            // ppIndexer.toTextFile("ppIndexer.txt");
            // weightCalc.toTextFile("weightcalc.txt");
            // ppIndexer.toTextFile("ppIndexer.txt");
            // tfIndexer.toTextFile("tfIndexer.txt");
            // forwardIndexer.toTextFile("forwardIndexer.txt"); 
            // bodyIndexer.toTextFile("bodyIndexer.txt");
            // titleIndexer.toTextFile("titleIndexer.txt");
 
            // ci.toTextFile("ci.txt");
            // pi.toTextFile("pi.txt");

            // // Scanner s = new Scanner(System.in); 
            // // System.out.println("Enter your query: "); 
            // // String query = s.nextLine(); 
            // // s.close(); 

            // StopStem ss = new StopStem("stopwords.txt");

            // //System.out.println(parsedQuery); 
            // CosSim cossim = new CosSim("./db/CosSimIndex", idManager, query, weightCalc, forwardIndexer, titleIndexer, bodyIndexer, ss);
            // cossim.calc();
            // cossim.toTextFile("cossimPrint.txt");
            // //cossim.printAll();
            
            // Retrieval r = new Retrieval(cossim); 
            // HashMap<Integer, String> top50pages = r.top50();
            // System.out.println(top50pages);
            // SearchEngine se = new SearchEngine(query, top50pages, cossim, pidIndexer, c, ppIndexer);
            // System.out.println(se.search());
            
        }
        catch (RocksDBException e) {
            System.out.println(e);
        }
        // SearchEngine se = new SearchEngine("717720");
        // System.out.println(se.search());
    }
}
