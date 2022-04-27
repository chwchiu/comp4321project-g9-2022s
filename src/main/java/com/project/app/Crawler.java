package com.project.app;
import java.util.Vector;
import java.util.HashSet;
import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import org.jsoup.HttpStatusException;
import java.util.regex.Pattern;
import org.rocksdb.RocksDB;
// import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
// import org.rocksdb.RocksIterator;
import javax.net.ssl.SSLHandshakeException; 
import java.net.*; 
 
// @SuppressWarnings("serial")
/** This is customized exception for those pages that have been visited before.
 */
class RevisitException
    extends RuntimeException {
    public RevisitException() {
        super();
    }
}

/** Crawler crawls pages starting from url
 */ 
public class Crawler {
    private HashSet<String> urls;     // the set of urls that have been visited before
    public Vector<Link> todos; // the queue of URLs to be crawled
    private int max_crawl_depth = 2;  // feel free to change the depth limit of the spider.
    public Parser p; 
   
    /** Crawler constructor
    */
    Crawler(String _url, Parser p) {
        this.todos = new Vector<Link>();
        this.todos.add(new Link(_url, 1));
        this.urls = new HashSet<String>();
        this.p = p; 
    }
   

    /**
     * Send an HTTP request and analyze the response, then send to Parser
     * @param url the url to check the response code of 
     * @return {Response} res
     * @throws HttpStatusException throws non-existing pages
     * @throws IOException I/O Exception
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

    /** NOTE: NOT USED RIGHT NOW DONT DELETE YET
        Used to check for redirects
        @param {link} the link to be checked
        @return the actual link
     */
    private String getActualLink(String link){
        try {
            URL url = new URL(link);
            HttpURLConnection http = (HttpURLConnection)url.openConnection();
            http.setInstanceFollowRedirects(false); 
            http.connect(); 
            int responseCode = http.getResponseCode();
            if (responseCode == 301 || responseCode == 302) 
                return http.getHeaderField("Location"); 
            return link; 
        } catch (SSLHandshakeException e) {
            return link; 
        } catch (IOException e) {
            return link;
        }
    }

    /** Extract useful external urls on the web page.
     * note: filter out images, emails, etc.
     * @param {Document} doc
     * @return {Vector<String>} a list of external links on the web page
     */
    private Vector<String> extractLinks(Document doc, Link focus) {
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

    /** Use a queue to manage crawl tasks.
     * @see Parser#parse(Response, String, Vector)
     */
    public void crawlLoop() {
        //init indexer
        while(!this.todos.isEmpty()) {
        
            Link focus = this.todos.remove(0);
            if (focus.level > this.max_crawl_depth) break; // stop criteria
            if (this.urls.contains(focus.url)) continue;   // ignore pages that has been visited
            /* start to crawl on the page */
            try {
                String actualURL = p.getActualLink(focus.url); 
                while (actualURL.charAt(actualURL.length() - 1) == '/') 
                    actualURL = actualURL.substring(0, actualURL.length() - 1); 

                
                Response res = this.getResponse(actualURL);
                Document doc = res.parse(); 
                Vector<String> links = this.extractLinks(doc, focus);
                
                p.parse(res, focus.url, links); 
                for(String link: links) {
                    this.todos.add(new Link(link, focus.level + 1)); // add links
                }
            } catch (SSLHandshakeException e) {
                System.out.printf("\nSSLHandshakeException: %s", focus.url);
            } catch (HttpStatusException e) {
                // e.printStackTrace ();
                System.out.printf("\nLink Error: %s\n", focus.url);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RevisitException e) {
                e.printStackTrace(); 
            } 
        }
    }
}