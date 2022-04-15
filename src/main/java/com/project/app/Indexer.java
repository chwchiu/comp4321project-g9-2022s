package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksIterator;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Indexer {
    // public String name = "";
    protected RocksDB db;

    Indexer(String dbPath) throws RocksDBException
    {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        Options options = new Options();
        options.setCreateIfMissing(true);

        // create and open the database
        this.db = RocksDB.open(options, dbPath);
    }

    /**
     * Returns String val associated with key, returns empty String if key not found
     * @param key
     * @return
     * @throws RocksDBException
     */
    public String getByKey(String key) throws RocksDBException
    {
        byte[] val = db.get(key.getBytes());
        if (val != null) return new String(val);
        else return "";
    }
    
    /**
     * Deletes an entry from db
     * @param word key
     * @throws RocksDBException
     */
    public void delEntry(String word) throws RocksDBException
    {
        db.delete(word.getBytes());
    }

    /**
     * Prints everything in the db
     * @throws RocksDBException
     */
    public void printAll() throws RocksDBException
    {
        RocksIterator iter = db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            //if(new String(iter.key()) == "") System.out.println("null key");
            System.out.println("key: " + new String(iter.key()) + " val: " + new String(iter.value()));
        }
    }

    /**
     * Alternative to printing everything, instead sends contents of db to a text file
     * @param word key
     * @throws RocksDBException
     */
    public void toTextFile(String filePath) throws RocksDBException
    {
        try(PrintWriter out = new PrintWriter(filePath)){
            RocksIterator iter = db.newIterator();
                        
            for(iter.seekToFirst(); iter.isValid(); iter.next()) {
                out.println("key: " + new String(iter.key()) + " val: " + new String(iter.value()));
            }
        } catch (FileNotFoundException e){
            System.err.println(e);
        }
    }
}
