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
        try{
            pid.addEntry(url.toLowerCase());
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

    /**
     * Debugging function to printAll both Page and Word ID Indexers
     * @throws RocksDBException
     * @see {@link Indexer#printAll()}
     */
    public void printAll() throws RocksDBException
    {
        pid.printAll();
        System.out.println("---------");
        wid.printAll();
    }

    public static void main(String[] args) {
        try{
            IDIndexer pidIndexer = new IDIndexer("./db/pidIndexer");
            IDIndexer widIndexer = new IDIndexer("./db/widIndexer");
            IDManager idm = new IDManager(pidIndexer, widIndexer);
            Vector<String> testVector = new Vector<String>();

            testVector.add("Hong");
            testVector.add("Kong");
            testVector.add("is");
            testVector.add("cool");

            idm.addWords(testVector);
            idm.addUrl("abc.com");

            idm.printAll();
        }
        catch (RocksDBException e){
            e.printStackTrace();
        }
        
    }

}
