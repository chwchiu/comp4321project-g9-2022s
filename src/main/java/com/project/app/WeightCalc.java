package com.project.app; 

import org.rocksdb.RocksDBException; 
import org.rocksdb.RocksIterator;
import java.util.*;

public class WeightCalc extends Indexer {
    protected TFIndexer TFDB;
    protected ForwardIndexer forwardDB; 
    protected InvertedIndexer titleDB;  //NOT USED FOR NOW ASK GROUP 
    protected InvertedIndexer bodyDB; 
    private IDManager idManager;

    /**
     * The constructor for the weight calculation class
     * @param weightdbPath the path for the weights
     * @param TFDB the tf db
     * @param forwardDB the forward db
     * @param titleDB the title db
     * @param bodyDB the body db
     * @param idManager the id db
     * @throws RocksDBException rocks db exception 
     */
    WeightCalc(String weightdbPath, TFIndexer TFDB, ForwardIndexer forwardDB, InvertedIndexer  titleDB, InvertedIndexer bodyDB, IDManager idManager) throws RocksDBException {
        super(weightdbPath); 
        this.TFDB = TFDB; 
        this.forwardDB = forwardDB; 
        this.titleDB = titleDB;
        this.bodyDB = bodyDB; 
        this.idManager = idManager;
    }

    /**
     * Helper function to get a hashmap of all the term frequencies
     * @param s string to parse
     * @return returns hashmap of all tf in a document
     */
    protected HashMap<String, Integer> parseTF(String s) {
        HashMap<String, Integer> wordFreq = new HashMap<String, Integer>(); 
        String temp = s;
        if (s.charAt(0) == ',' || s.charAt(0) == ' ') 
            temp = s.substring(1); 

        String[] wordFreqPairs = temp.split(","); 
        for (String w : wordFreqPairs) {
            String [] temp2 = w.split(":"); 
            wordFreq.put(temp2[0], Integer.parseInt(temp2[1])); 
        }
        return wordFreq; 
    }

    /**
     * Helper function to parse all the words out
     * @param s all the words in a document
     * @return returns a list of all the words in a document
     */
    protected ArrayList<String> parseWords(String s) {
        ArrayList<String> words = new ArrayList<String>(); 
        String temp = s; 
        if (s.charAt(0) == ',' || s.charAt(0) == ' ')
            temp = s.substring(1); 
    
        String [] wordArr = temp.split(",");
        
        for (String w : wordArr) 
            words.add(w); 

        return words; 
    }

    /**
     * Helper function to get the document frequency given a word
     * @param wordID the wordID of the word
     * @return int value of the document frequency
     */

    protected int getDocFreq(String wordID) throws RocksDBException{
        String bodyListOfDocs = new String(bodyDB.getByKey(wordID));   //Gets the list of documents with word in body ]
        String [] arr = bodyListOfDocs.split(" "); 
        return arr.length; 
    }


    /** Adds url, term frequency list key pair into tf database
     * @param doc the document 
     * @param weights list of all weights of the doc
     * @throws RocksDBException rocks db exception
     */
    public void addEntry(String doc, String weights) throws RocksDBException{
        byte[] content = weights.getBytes();
        db.put(doc.getBytes(), content); 
    }

    /** 
     * Function to process all the weights
     */
    public void processWeight() {  
        //Get total number of documents
        RocksIterator iter = forwardDB.db.newIterator(); 
        int docCount = 0; 
        for (iter.seekToFirst(); iter.isValid(); iter.next()) {
            docCount++; 
        }

        //Loop through all documents
        RocksIterator iter2 = forwardDB.db.newIterator(); 
        for (iter2.seekToFirst(); iter2.isValid(); iter2.next()) {                 
            HashMap<String, Double> weightsOfDoc = new HashMap<String, Double>();    
            String docID = new String(iter2.key()); 

            //Get list of all words in a doc
            ArrayList<String> wordList = parseWords(new String(iter2.value()));       
            //Get list of all term frequencies of the words in the doc
            try {
                HashMap<String, Integer> tfList = parseTF(new String(TFDB.getByKey(docID)));  
                //Loop through all the words and get the weight of each one
                for (String word : wordList) {
                    try {
                        int docFreq = getDocFreq(word); 
                        double idf = (Math.log(docCount / docFreq)) / (Math.log(2));   
                        int tf = tfList.get(word).intValue(); 
                        int tfMax = Collections.max(tfList.values()); 

                        double weight = idf * tf; 
                        double norm_weight = weight / tfMax;
                        weightsOfDoc.put(word, norm_weight); 
                    } catch (RocksDBException e) {
                        System.out.println("idManager getWordID rocksdbexception");
                    }
                }
                try {
                    String formattedNormWeights = "";
                    for (Map.Entry<String, Double> set : weightsOfDoc.entrySet()) {
                        formattedNormWeights = formattedNormWeights + "," + set.getKey() + ":" + String.format("%.02f", set.getValue());
                    }
                    formattedNormWeights = formattedNormWeights.substring(1); 
                    this.addEntry(docID, formattedNormWeights); 
                } catch (RocksDBException e) {
                    System.out.println("addentry rocksdb error");
                }
            } catch (RocksDBException e) {
                System.out.println("parseTF exception");
            }
        }
    }

}