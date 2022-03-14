package com.project.app;

/* --
COMP4321 Lab1 Exercise
Student Name:
Student ID:
Section:
Email:
*/

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class Indexer
{
    private RocksDB db;
    private Options options;

    Indexer(String dbPath) throws RocksDBException
    {
        // the Options class contains a set of configurable DB options
        // that determines the behaviour of the database.
        this.options = new Options();
        this.options.setCreateIfMissing(true);

        // create and open the database
        this.db = RocksDB.open(options, dbPath);
    }

    //deprecated addEntry, new addEntry below
    // public void addEntry(String word, int x, int y) throws RocksDBException
    // {
    //     // Add a "docX Y" entry for the key "word" into hashtable
    //     // ADD YOUR CODES HERE
    //     byte[] content = db.get(word.getBytes());
    //     if (content == null) {
    //         content = ("doc" + x + " " + y).getBytes();
    //     } else {
    //         content = (new String(content) + " doc" + x + " " + y).getBytes();
    //     }
    //     db.put(word.getBytes(), content);
    // }

    //just for phase one
    public void addEntry(String url, String data) throws RocksDBException
    {
        byte[] val = db.get(url.getBytes());
        if (val == null){
            val = data.getBytes();
        } else {
            val = (new String(val) + data).getBytes();
        }
        db.put(url.getBytes(), val);
    }

    public void delEntry(String word) throws RocksDBException
    {
        // Delete the word and its list from the hashtable
        // ADD YOUR CODES HERE
        db.delete(word.getBytes());
    } 
    public void printAll() throws RocksDBException
    {
        // Print all the data in the hashtable
        // ADD YOUR CODES HERE
        RocksIterator iter = db.newIterator();
                    
        for(iter.seekToFirst(); iter.isValid(); iter.next()) {
            System.out.println(new String(iter.key()) + "=" + new String(iter.value()));
        }
    }
    
    public static void main(String[] args)
    {
        // try
        // {
        //     // a static method that loads the RocksDB C++ library.
        //     RocksDB.loadLibrary();

        //     // modify the path to your database
        //     String path = "/root/comp4321project-g9-2022s/db";
            
        //     // Indexer index = new Indexer(path);
    
        //     // index.addEntry("cat", 2, 6);
        //     // index.addEntry("dog", 1, 33);
        //     // System.out.println("First print");
        //     // index.printAll();
            
        //     // index.addEntry("cat", 8, 3);
        //     // index.addEntry("dog", 6, 73);
        //     // index.addEntry("dog", 8, 83);
        //     // index.addEntry("dog", 10, 5);
        //     // index.addEntry("cat", 11, 106);
        //     // System.out.println("Second print");
        //     // index.printAll();
            
        //     // index.delEntry("dog");
        //     // System.out.println("Third print");
        //     // index.printAll();
        // }
        
        // catch(RocksDBException e)
        // {
        //     System.err.println(e.toString());
        // }
    }
}