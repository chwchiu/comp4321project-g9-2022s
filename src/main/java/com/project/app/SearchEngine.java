package com.project.app; 

import org.omg.PortableInterceptor.NON_EXISTENT;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException; 
import org.rocksdb.RocksIterator;
import java.lang.*;
import java.text.DecimalFormat;
import java.io.*; 
import java.util.*;
import java.util.Vector;
import java.util.HashSet;
import java.util.Set;
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

public class SearchEngine{
  String query;
  HashMap<Integer, String> top50; 
  protected CosSim cossim; 
  protected IDIndexer pidIndexer; 
  protected Crawler c; 
  protected PagePropertiesIndexer pp; 

  //Constructor takes in a query
  public SearchEngine(String query, HashMap<Integer, String> top50, CosSim cossim, IDIndexer pidIndexer, Crawler c, PagePropertiesIndexer pp){
    this.query = query;
    this.top50 = top50; 
    this.cossim = cossim; 
    this.pidIndexer = pidIndexer; 
    this.c = c; 
    this.pp = pp; 
  }
 

  //Return a string thats the result of the search
  //converts retrieval into string
  public String search(){
    String s = new String();

    RocksIterator iter = cossim.db.newIterator();
    RocksIterator iteration = pidIndexer.db.newIterator(); 
    RocksIterator iter2 = pp.db.newIterator();

    for (Map.Entry<Integer, String> entry : top50.entrySet()){
      String doc = entry.getValue();
      for(iter.seekToFirst(); iter.isValid(); iter.next()){
        String key = new String(iter.key()); 
        if (key.equals(doc)){
          String url = new String(); 
          for(iteration.seekToFirst(); iteration.isValid(); iteration.next()){
            String did = new String(iteration.value()); 
            did = "doc" + did; 
            if (did.equals(key)){
              url = new String (iteration.key());
              break; 
            }
          }

          //Connection conn = Jsoup.connect(url).fllowRedirects(false);
          //Response res; 
      
          String properties = new String(); 
          for (iter2.seekToFirst(); iter2.isValid(); iter2.next()){
            String did = new String(iter2.key());
            did = "doc" + did; 
            if (did.equals(key)){
              properties = new String(iter2.value());
            }
          }
          
          
          String score = new String(iter.value()); 
          Double df = Double.parseDouble(score);
          String score_f = String.format("%.6f", df);
          s = s + score_f + "\t" + entry.getValue() + "\n" + "\t" + "\t" + url + "\n" + "\t" + "\t" + properties + "\n";
          //s = s + entry.getKey() + ";" + entry.getValue() + ":" + new String(iter.value()) + " ";
        }
      }
    }
    return s;
  }
}
