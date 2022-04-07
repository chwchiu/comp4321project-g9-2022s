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
 
/** The data structure for the crawling queue.
 */
class Link{
    String url;
    int level;
    Link (String url, int level) {  
        this.url = url;
        this.level = level;
    }  
}
 
@SuppressWarnings("serial")
/** This is customized exception for those pages that have been visited before.
 */
class RevisitException
    extends RuntimeException {
    public RevisitException() {
        super();
    }
}
 
public class Crawler {
    private HashSet<String> urls;     // the set of urls that have been visited before
    public Vector<Link> todos; // the queue of URLs to be crawled
    private int max_crawl_depth = 2;  // feel free to change the depth limit of the spider.
 
 
    // modify the path to database
    public String path = "/root/comp4321project-g9-2022s/db";
   
    Crawler(String _url) {
        this.todos = new Vector<Link>();
        this.todos.add(new Link(_url, 1));
        this.urls = new HashSet<String>();
    }
   
    /**
     * Send an HTTP request and analyze the response.
     * @return {Response} res
     * @throws HttpStatusException for non-existing pages
     * @throws IOException
     */
    public Response getResponse(String url) throws HttpStatusException, IOException {
        if (this.urls.contains(url)) {
            throw new RevisitException(); // if the page has been visited, break the function
         }
       
        Connection conn = Jsoup.connect(url).followRedirects(false);
        // the default body size is 2Mb, to attain unlimited page, use the following.
        // Connection conn = Jsoup.connect(this.url).maxBodySize(0).followRedirects(false);
        Response res;
        try {
            /* establish the connection and retrieve the response */
             res = conn.execute();
             /* if the link redirects to other place... */
             if(res.hasHeader("location")) {
                 String actual_url = res.header("location");
                 if (this.urls.contains(actual_url)) {
                    throw new RevisitException();
                 }
                 else {
                     this.urls.add(actual_url);
                 }
             }
             else {
                 this.urls.add(url);
             }
        } catch (HttpStatusException e) {
            throw e;
        }
        /* Get the metadata from the result */
        String lastModified = res.header("last-modified");
        int size = res.bodyAsBytes().length;
        String htmlLang = res.parse().select("html").first().attr("lang");
        String bodyLang = res.parse().select("body").first().attr("lang");
        String lang = htmlLang + bodyLang;
        // System.out.printf("Last Modified: %s\n", lastModified);
        // System.out.printf("Size: %d Bytes\n", size);
        // System.out.printf("Language: %s\n", lang);
        return res;
    }
   
    /** Extract words in the web page content.
     * note: use StringTokenizer to tokenize the result
     * @param {Document} doc
     * @return {Vector<String>} a list of words in the web page body
     */
    public Vector<String> extractWords(Document doc) {
         Vector<String> result = new Vector<String>();
        // ADD YOUR CODES HERE
         String temp = doc.body().text();
         StringTokenizer s = new StringTokenizer(temp);
         while (s.hasMoreTokens()) {
             result.add(s.nextToken());
         }
         return result;
    }
   
    /** Extract useful external urls on the web page.
     * note: filter out images, emails, etc.
     * @param {Document} doc
     * @return {Vector<String>} a list of external links on the web page
     */
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
               if (!(result.contains(link)))
                   result.add(link);
           } else {
               if (link != "" && link.charAt(0) == '/') //To append
                   link = link.substring(1);
               
               String concatLink = focus.url + link;
               if (!(result.contains(concatLink))) {
                   result.add(concatLink);
                //    System.out.println(concatLink);
               }
           }
       }
       return result;
    }
   
   
    /** Use a queue to manage crawl tasks.
     */
    public void crawlLoop() {
        //init indexer
        RocksDB.loadLibrary();
        InvertedIndexer index;

        try{    
            index = new InvertedIndexer(path); //Indexer Initialization
            while(!this.todos.isEmpty()) {
            
                Link focus = this.todos.remove(0);
                if (focus.level > this.max_crawl_depth) break; // stop criteria
                if (this.urls.contains(focus.url)) continue;   // ignore pages that has been visited
                /* start to crawl on the page */
                try {
                    Response res = this.getResponse(focus.url);
                    Document doc = res.parse();

                    String title = doc.title();
                    String lastModified = res.header("last-modified");
                    int size = res.bodyAsBytes().length;
                    index.addEntry(focus.url, "Title: " + title + "\n");
                    index.addEntry(focus.url, "URL: " + focus.url + "\n");
                    index.addEntry(focus.url, "Last Mod: " + lastModified + " Size: " + size + "\n");

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
					index.addEntry(focus.url, "Words: " + formattedWordFreq + "\n");
           
                    Vector<String> links = this.extractLinks(doc, focus);
					index.addEntry(focus.url, "Links: " + "\n");

                    for(String link: links) {
						index.addEntry(focus.url, link + "\n");
                        this.todos.add(new Link(link, focus.level + 1)); // add links
                    }

                } catch (HttpStatusException e) {
                    // e.printStackTrace ();
                    System.out.printf("\nLink Error: %s\n", focus.url);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (RevisitException e) {
                    System.out.println("TESTING12311\n");
                } catch(RocksDBException e) {
                    System.err.println(e.toString());
                }
            }
            index.toTextFile("spider_result.txt");
        } catch (RocksDBException e){
			System.out.println("I died");
        }

    }
   
    public static void main (String[] args) {
        RocksDB.loadLibrary();
        String url = "https://cse.hkust.edu.hk/";
        Crawler crawler = new Crawler(url);
        crawler.crawlLoop();
    }
}
