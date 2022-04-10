package com.project.app;
import java.util.Vector;
import java.util.HashSet;
import java.util.StringTokenizer;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import org.jsoup.HttpStatusException;
import java.lang.RuntimeException;
import java.util.regex.Pattern;
import org.rocksdb.RocksDB;
import java.util.HashMap; 
// import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
// import org.rocksdb.RocksIterator;
import java.util.Map;
import javax.net.ssl.SSLHandshakeException; 
import java.net.*; 
import java.util.Arrays;

@SuppressWarnings("serial")
/** This is customized exception for those pages that have been visited before.
 */
class RevisitException
    extends RuntimeException {
    public RevisitException() {
        super();
    }
}

public class Parser {
    private IDManager idManager;
    private InvertedIndexer titleIndexer;
    private InvertedIndexer bodyIndexer;
    private ForwardIndexer forwardIndexer;
    private PagePropertiesIndexer ppIndexer;
    private StopStem stemmer;


    public Parser(IDIndexer pidIndexer, IDIndexer widIndexer, InvertedIndexer titleIndexer, InvertedIndexer bodyIndexer, ForwardIndexer forwardIndexer, PagePropertiesIndexer ppIndexer) {
        this.idManager = new IDManager(pidIndexer, widIndexer);
        this.titleIndexer = titleIndexer;
        this.bodyIndexer = bodyIndexer;
        this.forwardIndexer = forwardIndexer;
        this.ppIndexer = ppIndexer;
        this.stemmer = new StopStem("stopwords.txt");
    }

    /** Extract words in the web page content.
     * note: use StringTokenizer to tokenize the result
     * @param {Document} doc
     * @return {Vector<String>} a list of words in the web page body
     */
    private Vector<String> extractWords(Document doc) {
         Vector<String> result = new Vector<String>();
         String temp = doc.body().text();
         StringTokenizer s = new StringTokenizer(temp);

         while (s.hasMoreTokens()) {
             result.add(s.nextToken());
         }
         return result;
    }

    /**
     * Extracts words from stemmed body and stemmed title, is overload of {@link #extractWords(Document)}
     * @param doc
     * @param title
     * @return
     */
    private Vector<String> extractWords(String body, String title) {
        Vector<String> result = new Vector<String>();
        String temp = title.concat(body);
        StringTokenizer s = new StringTokenizer(temp);
        
        while (s.hasMoreTokens()) {
            result.add(s.nextToken());
        }
        return result;
    }

    /**
     * Method to get IDManager to add Page and Words to ID Index
     * @param doc
     * @param url
     * @see IDManager
     */
    private void manageIDs(String body, String title, String url){
        Vector<String> words = this.extractWords(body, title);
        idManager.addUrl(url);
        idManager.addWords(words);
    }

    /**
     * Performs document parsing and sends to relevant indexers
     * @param res
     * @param url
     * @param links
     */
    public void parse(Response res, String url, Vector<String> links) {
        try {
            RocksDB.loadLibrary();
            Document doc = res.parse();

            //stop stem
            String body = stemmer.ss(doc.body().toString());
            String title = stemmer.ss(doc.title());

            //Handle ID adding here
            manageIDs(body, title, url);

            //Handle adding to forward Index
            forwardIndexer.addEntry(url, body, title);
            
            //Handle adding to body
            bodyIndexer.addEntry(url, body);

            //Handle adding to title
            titleIndexer.addEntry(url, title);

            //Handle adding to page prop
            ppIndexer.addEntry(url, body, title);
            
            //------------ BELOW IS OLD PARSE -------------------------
            // String lastModified = res.header("last-modified");

            // if (lastModified == null) {
                //     System.out.println("ASDAKDJADKA"); 
            // }
            // int size = res.bodyAsBytes().length;
            // titleIndexer.addEntry(url, "Title: " + title + "\n");
            // titleIndexer.addEntry(url, "URL: " + url + "\n");
            // titleIndexer.addEntry(url, "Last Mod: " + lastModified + " Size: " + size + "\n");

            //CALC WORD FREQ
            // HashMap<String, Integer> wordFreq = new HashMap<String, Integer>(); 
            // for (String x : words) {
            //     String preprocessWord = x.replace(".", ""); 
            //     preprocessWord = preprocessWord.replace("[", "");
            //     preprocessWord = preprocessWord.replace("]", "");
            //     preprocessWord = preprocessWord.replace("(", "");
            //     preprocessWord = preprocessWord.replace(")", "");
            //     preprocessWord = preprocessWord.replace("…", ""); 

            //     if (wordFreq.containsKey(preprocessWord)) {
            //         // PART WHERE I INCREMENT THE VALUE 
            //         Integer temp = wordFreq.get(preprocessWord); 
            //         temp += 1; 
            //         wordFreq.replace(preprocessWord, temp); 
            //     } else {
            //         wordFreq.put(preprocessWord, 1); 
            //     }
            // }
            // System.out.println(wordFreq);
            // String formattedWordFreq = ""; 
            // for (Map.Entry<String, Integer> set: wordFreq.entrySet()) {
            //     formattedWordFreq = formattedWordFreq + set.getKey() + " " + set.getValue() + ";"; 
            // }
            // titleIndexer.addEntry(url, "Words: " + formattedWordFreq + "\n");
            // titleIndexer.addEntry(url, "Links: " + "\n");

            // for(String link: links) {
            //     titleIndexer.addEntry(url, link + "\n");
            // }

        } catch (SSLHandshakeException e) {
            System.out.printf("\nSSLHandshakeException: %s", url);
        } catch (HttpStatusException e) {
            System.out.printf("\nLink Error: %s\n", url);
            // e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RevisitException e) {
            System.out.printf("RevisitException: %s\n", url);
            e.printStackTrace(); 
        }
        catch(RocksDBException e) {
            System.err.println(e.toString());
        }
    }

}