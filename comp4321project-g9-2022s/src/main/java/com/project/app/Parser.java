package com.project.app;
import java.util.Vector;
import java.util.HashSet;
import java.util.Iterator;
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
// class RevisitException
//     extends RuntimeException {
//     public RevisitException() {
//         super();
//     }
// }

public class Parser {
    private IDManager idManager;
    private InvertedIndexer titleIndexer;
    private InvertedIndexer bodyIndexer;
    private ForwardIndexer forwardIndexer;
    private PagePropertiesIndexer ppIndexer;
    private StopStem stemmer;
    private TFIndexer tfIndexer; 
    private ParentIndexer parentIndexer; 
    private ChildIndexer childIndexer; 

    /**
     * Constructor for the parser
     * @param pidIndexer page id db
     * @param widIndexer word id db
     * @param titleIndexer title text db
     * @param bodyIndexer body text db
     * @param forwardIndexer forward db
     * @param ppIndexer page properties db
     * @param tfIndexer term frequency db
     */
    public Parser(IDIndexer pidIndexer, IDIndexer widIndexer, InvertedIndexer titleIndexer, InvertedIndexer bodyIndexer, ForwardIndexer forwardIndexer,PagePropertiesIndexer ppIndexer, TFIndexer tfIndexer, ParentIndexer parentIndexer, ChildIndexer childIndexer) {
        this.idManager = new IDManager(pidIndexer, widIndexer);
        this.titleIndexer = titleIndexer;
        this.bodyIndexer = bodyIndexer;
        this.forwardIndexer = forwardIndexer;
        this.ppIndexer = ppIndexer;
        this.stemmer = new StopStem("stopwords.txt");
        this.tfIndexer = tfIndexer; 
        this.parentIndexer = parentIndexer; 
        this.childIndexer = childIndexer; 
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
     * @return vector of all the words
     */
    protected Vector<String> extractWords(String body, String title) {
        Vector<String> result = new Vector<String>();
        String temp = title.concat(" " + body);
        StringTokenizer s = new StringTokenizer(temp);
        
        while (s.hasMoreTokens()) {
            result.add(s.nextToken());
        }
        return result;
    }

    
    /**
     * Extracts words String text, is overload of {@link #extractWords(Document)}
     * @param text the text that we have to extract words from 
     * @return Vector of all the words in the text
     */
    public Vector<String> extractWords(String text) {
        Vector<String> result = new Vector<String>();
        StringTokenizer s = new StringTokenizer(text);
        
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

    public String getActualLink(String link){
        // try {
        String linkStripPound = link.split("#")[0]; 
        linkStripPound = linkStripPound.split("\\?")[0]; 
            // URL url = new URL(linkStripPound);
            // HttpURLConnection http = (HttpURLConnection)url.openConnection();
            // http.setInstanceFollowRedirects(false); 
            // http.connect(); 
            // int responseCode = http.getResponseCode();
            // if (responseCode == 301 || responseCode == 302) {
            //     String redirectLink = http.getHeaderField("Location"); 
            //     redirectLink = link.split("#")[0];
            //     redirectLink = link.split("\\?")[0]; 
            //     return redirectLink; 
            // }
        return linkStripPound; 
        // } catch (SSLHandshakeException e) {
        //     String linkStripPound = link.split("#")[0]; 
        //     linkStripPound = linkStripPound.split("\\?")[0]; 
        //     return linkStripPound; 
        // } catch (IOException e) {
        //     String linkStripPound = link.split("#")[0]; 
        //     linkStripPound = linkStripPound.split("\\?")[0]; 
        //     return linkStripPound;
        // }
    }
    /**
     * Method for handling the parsing and inserting into the inverted indexers
     * @param url the url to insert
     * @param text the text that needs to be inserted
     * @param indexer the invereted indexer to handle the inserting
     */
    public void invertedIndexParseAndInsert(String url, String text, InvertedIndexer indexer){
        Vector<String> words = extractWords(text); //Extracts all words from body
        HashMap<String, String> wordPosition = new HashMap<String, String>(); 
        Integer bodyPos = 1; 
        for (String w : words) {
            String preprocessWord = w.replaceAll("[.\\[\\]\\(\\)…]", ""); 
            if (wordPosition.containsKey(preprocessWord)) {
                String currentEntry = wordPosition.get(preprocessWord).toLowerCase(); 
                wordPosition.replace(preprocessWord, currentEntry + "," + bodyPos); 
            } else {
                wordPosition.put(preprocessWord, Integer.toString(bodyPos)); 
            }
            bodyPos++; 
        }

        // DO STEMMING HERE
        HashMap<String, String> stemmedWordPos = stemmer.ss(wordPosition);

        for (Map.Entry<String, String> set: stemmedWordPos.entrySet()) {
            try {
                indexer.addEntry(url, set.getKey(), set.getValue()); 
            } catch (RocksDBException e) {
                System.err.println(e.toString());
            }
        }
    }

    /**
     * Method for handling the parsing and inserting into the forward indexers
     * @param url url to parse
     * @param body the text from the page body that needs to be inserted
     * @param title the text from the page title tha needs to be inserted 
     * @param forward the forward indexer to handle the inserting
     * @param tf the term frequency indexer to handle the inserting
     */
    public void forwardIndexAndTFParseAndInsert(String url, String body, String title, ForwardIndexer forward, TFIndexer tf){
        Vector<String> body_w = extractWords(body); 
        Vector<String> body_t = extractWords(title);
        Vector<String> words = new Vector<String>(); 
        Vector<String> parsedWords = new Vector<String>(); 
        HashMap<String, Integer> wordFreq = new HashMap<String, Integer>(); 
        
        words.addAll(body_t);
        words.addAll(body_w);
        for (String w : words) { //Parse words and word freq
            String preprocessWord = w.replaceAll("[.\\[\\]\\(\\)…]", ""); 
            if (!(parsedWords.contains(preprocessWord)))  //Get preprocessed words
                parsedWords.add(preprocessWord); 
            
            if (wordFreq.containsKey(preprocessWord)) {  //Calculate word frequency at the same time
                Integer temp = wordFreq.get(preprocessWord); 
                temp += 1; 
                wordFreq.replace(preprocessWord, temp); 
            } else {
                wordFreq.put(preprocessWord, 1); 
            }
        }

        
        Iterator<String> iterate = parsedWords.iterator();  //Adding to the forward index

        try {
            tf.addEntry(url, wordFreq); //Index word frequency

            while(iterate.hasNext()){
                forward.addEntry(url, iterate.next());
            }
        } catch(RocksDBException e) {
            System.err.println(e.toString());
        } 
    }
    
    /**
     * Performs document parsing and sends to relevant indexers
     * @param res  the response of the connection
     * @param url  the url to parse
     * @param links the child links for the url
     */
    public void parse(Document d, String url, Vector<String> links, String lastModified, String size) {
        try {
            RocksDB.loadLibrary();
            Document doc = d;
            
            String actualURL = url;

            //stop stem
            // System.out.println(actualURL);
            String body = stemmer.ss(doc.body().text());
            String title = stemmer.ss(doc.title());

            //Handle ID adding here
            manageIDs(body, title, actualURL);
            
            if (forwardIndexer.getByKey(idManager.getUrlId(actualURL)) == "") {
                Vector<String> actualLinks = new Vector<>();
                for (String link : links) {
                    String actualLink = getActualLink(link);
                    idManager.addUrl(actualLink);
                    actualLinks.add(actualLink); 
                }

                if (idManager.getUrlId(actualURL) != "") {
                    //Handle adding to forward Index And Term Frequency
                    forwardIndexAndTFParseAndInsert(actualURL, body, title, forwardIndexer, tfIndexer);

                    //Handle adding to body
                    invertedIndexParseAndInsert(actualURL, doc.body().text(), bodyIndexer); 
                
                    //Handle adding to title
                    invertedIndexParseAndInsert(actualURL, doc.title(), titleIndexer); 

                    //Handle adding to page prop
                    ppIndexer.addEntry(actualURL, lastModified, size, doc.title());

                    parentIndexer.addEntry(actualLinks, actualURL);
                    childIndexer.addEntry(actualLinks, actualURL); 
                } else {
                    System.out.println("null key:" + actualURL); 
                }
            }
        } catch (RevisitException e) {
            System.out.printf("RevisitException: %s\n", url);
            // e.printStackTrace();
        }
        catch(RocksDBException e) {
            System.err.println(e.toString());
        }
    }

    /**
     * Function to assist with parsing the input
     * @param input the input to turn into a vector
     * @return returns a parsed vector of the input
     */
    public Vector<String> parseInput(String input)
    {
        //parse input and stem it
        String stemmedInput = stemmer.ss(input);
        return extractWords(stemmedInput);
    }
} 