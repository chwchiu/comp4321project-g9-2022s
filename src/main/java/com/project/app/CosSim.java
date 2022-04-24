package com.project.app; 

import org.omg.PortableInterceptor.NON_EXISTENT;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException; 
import org.rocksdb.RocksIterator;
import java.lang.*;
import java.io.*; 
import java.util.*;

public class CosSim extends Indexer{
    private IDManager idManager;
    protected ForwardIndexer forwardDB; 
    protected InvertedIndexer titleDB;  
    protected InvertedIndexer bodyDB; 
    protected WeightCalc weighter; 
    protected String query; 
    private Parser parser; 

    CosSim(String dbPath, IDManager idManager, String query, WeightCalc weighter, ForwardIndexer forwardDB, InvertedIndexer  titleDB, InvertedIndexer bodyDB, Parser parser) throws RocksDBException
    {
        super(dbPath);
        this.idManager = idManager;
        this.forwardDB = forwardDB;
        this.titleDB = titleDB;
        this.bodyDB = bodyDB; 
        this.query = query; 
        this.parser = parser;
        this.weighter = weighter;  
    }

    private HashMap<String, String> docs(Integer indexer, String WID) throws RocksDBException
    {
        String bodyListOfDocs = new String(bodyDB.getByKey(WID));
        HashMap<String, String> weights = new HashMap<String, String>();
        if (bodyListOfDocs.isEmpty()){
            return weights; 
        }
        
        String [] arr = bodyListOfDocs.split(" ");
        for (int w = 0; w < arr.length; w++){
            String[] temp = arr[w].split("#");
            weights.put(temp[0], temp[1]);
        }   
        return weights; 
        
    }
    private HashMap<String, String> docs_t(Integer indexer, String WID) throws RocksDBException
    {
        String bodyListOfDocs = new String(titleDB.getByKey(WID));
        HashMap<String, String> weights = new HashMap<String, String>();
        if (bodyListOfDocs.isEmpty()){
            return weights; 
        }
        
        String [] arr = bodyListOfDocs.split(" ");
        for (int w = 0; w < arr.length; w++){
            String[] temp = arr[w].split("#");
            weights.put(temp[0], temp[1]);
        }   
        return weights; 
        
    }



    public void calc() throws RocksDBException{ 
        Vector<String> body_w = parser.extractWords(query); //Extract the words from the query 
        Vector<String> q_WID = new Vector<String>(); //To store the query Word Ids 

        for (int index = 0; index < body_w.size(); index++){ //Finding the query word IDs 
            String temp = idManager.getWordId(body_w.get(index));
            if (temp == " "){
                continue; 
            }
            q_WID.add(temp);
         }
         
         RocksIterator iter = weighter.db.newIterator(); 

         for (iter.seekToFirst(); iter.isValid(); iter.next()) { 
             String docs = new String(iter.key()); //Gets key 
             docs = "doc" + docs; 
             //System.out.println(docs);
             String wording = new String(iter.value());
             String[] words = wording.split(","); //Get value and split it into array which contains all the wordID + weights
             Double magnitude_b = 0.0; 
             Double magnitude_t = 0.0; 
             Double score_b = 0.0; 
             Double score_t = 0.0; 
             Double score = 0.0; 

             for (int indexer = 0; indexer<q_WID.size(); indexer++)
             {
                HashMap<String, String> docu = docs(indexer, q_WID.get(indexer));
                if (docu.isEmpty()){
                    continue;
                }
                if (docu.containsKey(docs)){
                    for (int i = 0; i < words.length; i++){
                        String[] weight = words[i].split(":");
                        double value = Double.parseDouble(weight[1]);
                        if (q_WID.contains(weight[0])){ //The inner product 
                            score_b = score_b + value; 
                        }
                        magnitude_b = magnitude_b + Math.pow(value, 2); // Magnitude calculation 
                    }
                }
             }
             Double cossim_b = score_b/(Math.sqrt(magnitude_b)); //Cosine similarity of the body 
             if (cossim_b.isNaN()){
                 cossim_b = 0.0; 
             }
             for (int indexer = 0; indexer<q_WID.size(); indexer++)
             {
                HashMap<String, String> docu = docs_t(indexer, q_WID.get(indexer));
                if (docu.isEmpty()){
                    continue;
                }
                if (docu.containsKey(docs)){
                    for (int i = 0; i < words.length; i++){
                        String[] weight = words[i].split(":");
                        double value = Double.parseDouble(weight[1]);
                        if (q_WID.contains(weight[0])){ //The inner product 
                            score_t = score_t + value; 
                        }
                        magnitude_t = magnitude_t + Math.pow(value, 2); // Magnitude calculation 
                    }
                }
             }

             Double cossim_t = score_t/Math.sqrt(magnitude_t);
             if (cossim_t.isNaN()){
                 cossim_t = 0.0; 
             }

             score = cossim_b + 2*cossim_t; 
             //Integer ip = value; 
             //String sum = "0"; //Set total sum to 0 for now 
             db.put(docs.getBytes(), (Double.toString(score)).getBytes());
         }

      
    }
}



