//IMPORTANT READ!
//MAKE SURE TO REDO ADDENTRY FOR PROJECT!
package com.project.app;

import org.rocksdb.RocksDB;
import org.rocksdb.Options;
import org.rocksdb.RocksIterator;
import org.rocksdb.RocksDBException;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class InvertedIndexer extends Indexer
{
    private RocksDB db;
    private Options options;

    InvertedIndexer(String dbPath) throws RocksDBException
    {
        super(dbPath);
    }

    @Override
    public void addEntry(String word, int x, int y) throws RocksDBException
    {
        // Add a "docX Y" entry for the key "word" into hashtable
        byte[] content = db.get(word.getBytes());
        if (content == null) {
            content = ("doc" + x + " " + y).getBytes();
        } else {
            content = (new String(content) + " doc" + x + " " + y).getBytes();
        }
        db.put(word.getBytes(), content);
    }

    // public static void main(String[] args)
    // {
    //     try
    //     {
    //         // a static method that loads the RocksDB C++ library.
    //         RocksDB.loadLibrary();

    //         // modify the path to your database
    //         String path = "/root/comp4321project-g9-2022s/db";
            
    //         InvertedIndexer index = new InvertedIndexer(path);
    
    //         index.addEntry("cat", 2, 6);
    //         index.addEntry("dog", 1, 33);
    //         System.out.println("First print");
    //         index.printAll();
            
    //         index.addEntry("cat", 8, 3);
    //         index.addEntry("dog", 6, 73);
    //         index.addEntry("dog", 8, 83);
    //         index.addEntry("dog", 10, 5);
    //         index.addEntry("cat", 11, 106);
    //         System.out.println("Second print");
    //         index.printAll();
            
    //         index.delEntry("dog");
    //         System.out.println("Third print");
    //         index.printAll();
    //     }
        
    //     catch(RocksDBException e)
    //     {
    //         System.err.println(e.toString());
    //     }
    // }
}