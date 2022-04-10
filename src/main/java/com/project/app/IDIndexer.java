package com.project.app;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class IDIndexer extends Indexer {
    private Integer counter = 0;

    IDIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
        
        RocksIterator iter = db.newIterator();
        for (iter.seekToFirst(); iter.isValid(); iter.next()) counter++;
    }

    public boolean addEntry(String word) throws RocksDBException
    {
        byte[] content = db.get(word.getBytes());
        if (content == null){
            db.put(word.getBytes(), counter.toString().getBytes());
            counter++;
            return true;
        }
        else return false;
    }

    public String getKeyfromVal(String val) throws RocksDBException
    {
        RocksIterator iter = db.newIterator();
        for(iter.seekToFirst(); iter.isValid(); iter.next()){
            if(new String(iter.value()) == val) return new String(iter.key());
        }
        return "";
    }
    
    public static void main(String[] args) {
        try
        {
            //setup all dbs
            IDIndexer pidIndexer = new IDIndexer("./db/PageIDIndex");
            IDIndexer widIndexer = new IDIndexer("./db/WordIDIndex");
            IDManager idManager = new IDManager(pidIndexer, widIndexer);

            InvertedIndexer bodyIndexer = new InvertedIndexer("./db/BodyIndex", idManager);

            idManager.addUrl("abc.com");
            idManager.addUrl("def.com");
            bodyIndexer.addEntry("abc.com", "abc");
            bodyIndexer.addEntry("def.com", "def");

            System.out.println("bi");
            bodyIndexer.printAll();
            idManager.printAll();
        }
        catch (RocksDBException e)
        {
            e.printStackTrace();
        }


    }
}
