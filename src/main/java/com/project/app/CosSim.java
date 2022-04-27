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
    protected Vector<String> query; 
    private Parser parser; 

    CosSim(String dbPath, IDManager idManager, Vector<String> query, WeightCalc weighter, ForwardIndexer forwardDB, InvertedIndexer  titleDB, InvertedIndexer bodyDB) throws RocksDBException
    {
        super(dbPath);
        this.idManager = idManager;
        this.forwardDB = forwardDB;
        this.titleDB = titleDB;
        this.bodyDB = bodyDB; 
        this.query = query; 
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
        Vector<String> q_WID = new Vector<String>(); //To store the query Word Ids 
        for (int index = 0; index < query.size(); index++){ //Finding the query word IDs 
            
            Vector<String> phrase = new Vector<String>(); 
            String[] phrases = query.get(index).split(" "); //If it's a phrase, then we can separate it 
            for (int indexing = 0; indexing < phrases.length; indexing++){
                String temp = idManager.getWordId(phrases[indexing]);
                if (temp == ""){
                    System.out.println("no match");
                    return;  //If the phrase is non existent, no matches will be returned 
                }
                phrase.add(temp); //Store the WID 
            }
            String fin = phrase.get(0);
            if (phrase.size() > 1){
            for (int all = 1; all < phrase.size(); all++){
                fin = fin + " " + phrase.get(all);
            }}
            q_WID.add(fin); //Adding the WID of the phrase or individual word to the vector 
        }
            
         RocksIterator iter = weighter.db.newIterator(); 

         for (iter.seekToFirst(); iter.isValid(); iter.next()) { 
             
             String docs = new String(iter.key()); //Gets key 
             docs = "doc" + docs; 
             String values = new String(iter.value()); //The weight of the word 
             String[] ind = values.split(","); //Get value and split it into array which contains all the wordID + weights
             ArrayList<String> wid = new ArrayList<String>(); //Stores all the WID in a doc 
             ArrayList<String> weight = new ArrayList<String>(); //Stores all the weight of the words in a doc 

             for (int i = 0; i < ind.length; i++){
                 String[] temp = ind[i].split(":");
                 wid.add(temp[0]); 
                 weight.add(temp[1]);
             }
             //Initialize magnitude and inner products + final cossim
             Double magnitude = 0.0;
             for (int w = 0; w < weight.size(); w++){
                Double value = Double.parseDouble(weight.get(w));
                magnitude = magnitude + Math.pow(value, 2);
             } 
             Double score_b = 0.0; 
             Double score_t = 0.0; 
             Double score = 0.0; 

             for (int q = 0; q < q_WID.size(); q++){   //Iterate through the query words 
                String[] temp_q = q_WID.get(q).split(" "); //To get the individual words in the query 

                if (temp_q.length > 1){ //So if it is a phrase 
                    double temp_score = 0.0;
                    double temp_score_t = 0.0; 
                    int counter = 0; 
                    int counter_t = 0; 
                    for (int l = 0; l < temp_q.length; l++){ //Iterates through all the words in the phrase 
                        HashMap<String, String> documents = docs(q, temp_q[l]); //Hashmap of all the docs which has the word 
                        HashMap<String, String> documents_next = new HashMap<String, String>(); 
                        if (l < temp_q.length-1){ //If it's not the last word, find the next word's doc list + position 
                            documents_next = docs(q, temp_q[l+1]);}
                            
                        
                        if (l == temp_q.length-1){ //If we are on the last word 
                            if (documents.containsKey(docs)){
                                int index = wid.indexOf(temp_q[l]); 
                                
                                temp_score = temp_score + Double.parseDouble(weight.get(index));
                                counter = counter + 1; 
                            }
                        }
                        if (documents.containsKey(docs) & documents_next.containsKey(docs)) { //If the word and the next word is present in the document
                            int[] doc_words = Arrays.stream(documents.get(docs).split(",")).mapToInt(Integer::parseInt).toArray(); //Get the position of the current word 
                            int[] next_words =  Arrays.stream(documents_next.get(docs).split(",")).mapToInt(Integer::parseInt).toArray(); //Get the position of the next word
                            for (int i = 0; i < doc_words.length; i++){ 
                                for (int j = 0; j < next_words.length; j++){
                                    if (doc_words[i] == next_words[j]-1){
                                        int index = wid.indexOf(temp_q[l]); //Get the index of the WID 
                                        temp_score = temp_score + Double.parseDouble(weight.get(index)); //Get the weight of the word 
                                        counter = counter + 1; 
                                    }
                                    if(doc_words[i] > next_words[j]){
                                        continue; 
                                    }
                                    if(doc_words[i] < next_words[j]-1){
                                        break; 
                                    }
                                }
                            }
                        }
                        
                        HashMap<String, String> documents_t = docs_t(q, temp_q[l]);
                        HashMap<String, String> documents_next_t = new HashMap<String, String>();
                        if (l < temp_q.length-1){
                            documents_next_t = docs_t(q, temp_q[l+1]);
                        }

                        if (l == temp_q.length-1){
                            if (documents.containsKey(docs)){
                                int index = wid.indexOf(temp_q[l]);
                                temp_score_t = temp_score_t + Double.parseDouble(weight.get(index));
                                counter_t = counter_t + 1; 
                            }
                        }

                        if (documents_t.containsKey(docs) & documents_next_t.containsKey(docs)){
                            int [] doc_words_t = Arrays.stream(documents_t.get(docs).split(",")).mapToInt(Integer::parseInt).toArray();
                            int [] next_words_t = Arrays.stream(documents_next_t.get(docs).split(",")).mapToInt(Integer::parseInt).toArray();
                            for (int i = 0; i < doc_words_t.length; i++){
                                for (int j = 0; j < next_words_t.length; j++){
                                    if (doc_words_t[i] == next_words_t[j]-1){
                                        int index = wid.indexOf(temp_q[l]);
                                        temp_score_t = temp_score_t + Double.parseDouble(weight.get(index));
                                        counter_t = counter + 1; 
                                    }
                                    if(doc_words_t[i] > next_words_t[j]){
                                        continue;
                                    }
                                    if (doc_words_t[i] < next_words_t[j]-1){
                                        break; 
                                    }
                                }
                            }
                        }
                          

                    }
                    if (counter == temp_q.length || counter_t == temp_q.length){
                        score_b = temp_score / temp_q.length; //The inner product becomes the average weight of all of the terms in the phrase 
                        score_t = temp_score_t / temp_q.length; 
                    }
                    else {
                        score = 0.0; 
                        break; 
                    }
                    continue; 
                }

                else { 
                    if (wid.contains(q_WID.get(q))){ //If the doc contains the query word 
                        HashMap<String, String> documents = docs(q, q_WID.get(q));
                        if (documents.containsKey(docs)){
                            int index = wid.indexOf(q_WID.get(q));
                            score_b = score_b + Double.parseDouble(weight.get(index));
                        }

                        HashMap<String, String> documents_t = docs_t(q, q_WID.get(q));
                        if (documents_t.containsKey(docs)){
                            int index = wid.indexOf(q_WID.get(q));
                            score_t = score_t + Double.parseDouble(weight.get(index));
                        }
                    }
                }
             }

             Double cossim_b = score_b / Math.sqrt(magnitude);
             if (cossim_b.isNaN()){
                 cossim_b = 0.0; 
             }
             Double cossim_t = score_t / Math.sqrt(magnitude);
             if (cossim_t.isNaN()){
                 cossim_t = 0.0; 
             }

             score = 5*cossim_t + cossim_b; 
             db.put(docs.getBytes(), (Double.toString(score)).getBytes());

    }
}}



