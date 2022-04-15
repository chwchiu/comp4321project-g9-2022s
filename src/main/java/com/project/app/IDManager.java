package com.project.app;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

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
     * @param words
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
     * @param url
     * @see IDIndexer#addEntry(String)
     */
    public void addUrl(String url)
    {
        if(url == null) return;
        if(url == "") return;
        try{
            pid.addEntry(url);
        }
        catch(RocksDBException e){
            e.printStackTrace();
        }
    }
    
    /**
     * Gets id of URL by passing a String url into it, returns an empty string if no id found.
     * @param url
     * @return String pageID
     */
    public String getUrlId(String url) throws RocksDBException
    {
        return pid.getByKey(url);
    }

    /**
     * Gets id of word by passing a String word into it, returns an empty string if no id found.
     * @param url
     * @return String wordID
     */
    public String getWordId(String word) throws RocksDBException
    {
        return wid.getByKey(word);
    }

    public String getUrlFromId(String id) throws RocksDBException
    {
        return pid.getKeyfromVal(id);
    }

    /**
     * Debugging function to printAll both Page and Word ID Indexers
     * @throws RocksDBException
     * @see {@link Indexer#printAll()}
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
     * @see {@link Indexer#toTextFile()}
     */
    public void toTextFile(String pidPath, String widPath) throws RocksDBException
    {
        pid.toTextFile(pidPath);
        wid.toTextFile(widPath);
    }

    //testing
    // public static void main(String[] args) {
    //     try{
    //         IDIndexer pidIndexer = new IDIndexer("./db/pidIndexer");
    //         IDIndexer widIndexer = new IDIndexer("./db/widIndexer");
    //         IDManager idManager = new IDManager(pidIndexer, widIndexer);
    //         PagePropertiesIndexer ppIndexer = new PagePropertiesIndexer("./db/ppIndexer", idManager);
    //         PagePropertiesIndexer ppIndexer2 = new PagePropertiesIndexer("./db/ppIndexer2", idManager);
    //         Vector<String> testVector = new Vector<String>();

    //         testVector.add("Hong");
    //         testVector.add("Kong");
    //         testVector.add("is");
    //         testVector.add("cool");

    //         idManager.addWords(testVector);
    //         idManager.addUrl("abc.com");
    //         idManager.addUrl("def.com");

    //         idManager.printAll();

    //         System.out.println("-------");

    //         ppIndexer.addEntry("abc.com", "abc");
    //         ppIndexer2.addEntry("def.com", "def");
    //         ppIndexer.addEntry(".com", "nothin");
    //         ppIndexer.printAll();
    //         System.out.println("-------");
    //         ppIndexer2.printAll();
    //     }
    //     catch (RocksDBException e){
    //         e.printStackTrace();
    //     }
    // }

}
