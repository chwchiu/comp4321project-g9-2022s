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
    private Indexer i1;
    private InvertedIndexer i2;
    public Parser(Indexer i1, InvertedIndexer i2) {
        this.i1 = i1;
        this.i2 = i2; 
    }

    /** Extract words in the web page content.
     * note: use StringTokenizer to tokenize the result
     * @param {Document} doc
     * @return {Vector<String>} a list of words in the web page body
     */
    private Vector<String> extractWords(Document doc) {
         Vector<String> result = new Vector<String>();
        // ADD YOUR CODES HERE
         String temp = doc.body().text();
         StringTokenizer s = new StringTokenizer(temp);
         while (s.hasMoreTokens()) {
             result.add(s.nextToken());
         }
         return result;
    }

    public Vector<String> extractLinks(Document doc, Link focus) {
        Vector<String> result = new Vector<String>();
        // ADD YOUR CODES HERE
        Elements links = doc.select("a[href]");
        for (Element e : links) {
            String link = e.attr("href");
            if (link.contains("mailto"))
                continue;
            
            String regex = "^(http|https|ftp)://.*$";
            if (Pattern.matches(regex, link)) {
                //String actualLink = getActualLink(link); 
                if (!(result.contains(link)))
                    result.add(link);
            } else {
                if (link != "" && link.charAt(0) == '/') //To append
                    link = link.substring(1);
                
                String concatLink = focus.url + link;
                //String actualLink = getActualLink(concatLink); 
                if (!(result.contains(concatLink))) {
                    result.add(concatLink);
                //    System.out.println(concatLink);
                }
            }
        }
        return result;
    }

    public void parse(Response res, String url, Vector<String> links) {
        //init indexer
        //RocksDB.loadLibrary();
        try {
            RocksDB.loadLibrary();
            Document doc = res.parse();

            String title = doc.title();
            String lastModified = res.header("last-modified");
            // if (lastModified == null) {
            //     System.out.println("ASDAKDJADKA"); 
            // }
            int size = res.bodyAsBytes().length;
            i2.addEntry(url, "Title: " + title + "\n");
            i2.addEntry(url, "URL: " + url + "\n");
            i2.addEntry(url, "Last Mod: " + lastModified + " Size: " + size + "\n");

            Vector<String> words = this.extractWords(doc);

            //CALC WORD FREQ
            HashMap<String, Integer> wordFreq = new HashMap<String, Integer>(); 
            for (String x : words) {
                String preprocessWord = x.replace(".", ""); 
                preprocessWord = preprocessWord.replace("[", "");
                preprocessWord = preprocessWord.replace("]", "");
                preprocessWord = preprocessWord.replace("(", "");
                preprocessWord = preprocessWord.replace(")", "");
                preprocessWord = preprocessWord.replace("…", ""); 
            

                if (wordFreq.containsKey(preprocessWord)) {
                    // PART WHERE I INCREMENT THE VALUE 
                    Integer temp = wordFreq.get(preprocessWord); 
                    temp += 1; 
                    wordFreq.replace(preprocessWord, temp); 
                } else {
                    wordFreq.put(preprocessWord, 1); 
                }
            }
            // System.out.println(wordFreq);
            String formattedWordFreq = ""; 
            for (Map.Entry<String, Integer> set: wordFreq.entrySet()) {
                formattedWordFreq = formattedWordFreq + set.getKey() + " " + set.getValue() + ";"; 
            }
            i2.addEntry(url, "Words: " + formattedWordFreq + "\n");
            i2.addEntry(url, "Links: " + "\n");

            for(String link: links) {
                System.out.println("IN HERE");
                i2.addEntry(url, link + "\n");
            }

        } catch (SSLHandshakeException e) {
            System.out.printf("\nSSLHandshakeException: %s", url);
        } catch (HttpStatusException e) {
            // e.printStackTrace ();
            System.out.printf("\nLink Error: %s\n", url);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RevisitException e) {
            System.out.println("TESTING12311\n");
            e.printStackTrace(); 
        } catch(RocksDBException e) {
            System.err.println(e.toString());
        }

        try {
            i2.toTextFile("spider_result2.txt");
        } catch (RocksDBException e) {
            System.err.println(e.toString());
        }
    }

}