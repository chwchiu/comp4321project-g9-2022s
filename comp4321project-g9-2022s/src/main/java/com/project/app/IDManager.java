package com.project.app;
import org.rocksdb.RocksDBException;

import java.util.Vector;

public class IDManager{
    private IDIndexer pid;
    private IDIndexer wid;

    IDManager(IDIndexer pidIndexer, IDIndexer widIndexer)
    {
        this.pid = pidIndexer;
        this.wid = widIndexer;
    }

    /**
     * Creates an ID for new words, nothing happens if a word has an ID
     * @param words the list words to add
     */
    public void addWords(Vector<String> words)
    {
        words.forEach(word -> {
            try
            {
                wid.addEntry(word.toLowerCase());
            }
            catch (RocksDBException e){
                e.printStackTrace();
            }
        });
    }

    /**
     * Creates an ID for new urls, nothing happens if url already has an ID
     * @param url the url to add into db
     * @see IDIndexer#addEntry(String)
     */
    public void addUrl(String url)
    {
        if(url == null || url == "") return;
        try{
            pid.addEntry(url);
        }
        catch(RocksDBException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Gets id of URL by passing a String url into it, returns an empty string if no id found.
     * @param url the url 
     * @return String pageID
     * @throws RocksDBException rocks db exception
     */
    public String getUrlId(String url) throws RocksDBException
    {
        return pid.getByKey(url);
    }

    /**
     * Gets id of word by passing a String word into it, returns an empty string if no id found.
     * @param word the word 
     * @return String wordID
     * @throws RocksDBException rocks db excception
     */
    public String getWordId(String word) throws RocksDBException
    {
        return wid.getByKey(word);
    }

    /**
     * Gets url from id
     * @param id the id
     * @return the url
     * @throws RocksDBException rocks db exception
     */
    public String getUrlFromId(String id) throws RocksDBException
    {
        return pid.getKeyfromVal(id);
    }

    /**
     * Debugging function to printAll both Page and Word ID Indexers
     * @throws RocksDBException rocks db exception
     * @see Indexer#printAll()
     */
    public void printAll() throws RocksDBException
    {
        System.out.println("PIDs");
        pid.printAll();
        System.out.println("WIDs");
        wid.printAll();
    }

    /**
     * Creates 2 text files
     * @param pidPath path for first text file
     * @param widPath path for second text file
     * @see Indexer#toTextFile
     * @throws RocksDBException rocks db exception
     */
    public void toTextFile(String pidPath, String widPath) throws RocksDBException
    {
        pid.toTextFile(pidPath);
        wid.toTextFile(widPath);
    }

}
