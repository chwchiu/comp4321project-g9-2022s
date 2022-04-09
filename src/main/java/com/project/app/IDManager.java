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

    public void addURL(String url)
    {
        try{
            pid.addEntry(url.toLowerCase());
        }
        catch(RocksDBException e){
            e.printStackTrace();
        }
    }

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
            idm.addURL("abc.com");

            idm.printAll();
        }
        catch (RocksDBException e){
            e.printStackTrace();
        }
        
    }

}
