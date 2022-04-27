package com.project.app;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rocksdb.RocksDBException;
import org.rocksdb.RocksDB;
/**
 * Unit test for project
 */
public class AppTest 
{
    public static IDManager testidManager; 
    public static IDIndexer testpidIndexer;
    public static IDIndexer testwidIndexer;
    public static Crawler crawler; 
    public static InvertedIndexer testbodyIndexer;
    public static InvertedIndexer testtitleIndexer; 
    public static ForwardIndexer testforwardIndexer; 
    public static PagePropertiesIndexer testppIndexer; 
    public static TFIndexer testtfIndexer; 
    public static WeightCalc testweightCalc; 
    public static ChildIndexer testci; 
    public static ParentIndexer testpi; 
    public static Parser p;
    public static CosSim testcs; 
    public static Retrieval r; 
    public static StopStem testss; 
    @BeforeClass                                         
    public static void setUp() throws RocksDBException {
        testpidIndexer = new IDIndexer("./testdb/testTestPageIDIndex");
        testwidIndexer = new IDIndexer("./testdb/testTestWordIDIndex");
        testidManager = new IDManager(testpidIndexer, testwidIndexer);
        testppIndexer = new PagePropertiesIndexer("./testdb/testPagePropertiesIndex", testidManager);
        testbodyIndexer = new InvertedIndexer("./testdb/testBodyIndex", testidManager);
        testtitleIndexer = new InvertedIndexer("./testdb/testTitleIndex", testidManager);
        testforwardIndexer = new ForwardIndexer("./testdb/testForwardIndex", testidManager);
        testtfIndexer = new TFIndexer("./testdb/testTFIndex", testidManager);
        testci = new ChildIndexer("./testdb/testChildIndex", testidManager);
        testpi = new ParentIndexer("./testdb/testParentIndex", testidManager);
        p = new Parser(testpidIndexer, testwidIndexer, testtitleIndexer, testbodyIndexer, testforwardIndexer, testppIndexer, testtfIndexer, testpi, testci);
        crawler = new Crawler("www.cse.ust.hk", p); 
        testweightCalc = new WeightCalc("./testdb/testWeightIndex", testtfIndexer, testforwardIndexer, testtitleIndexer, testbodyIndexer, testidManager); 
        String testquery = "Hong Kong \"is cool\""; 
        testcs = new CosSim("./testdb/testCosSimIndex", testidManager, testquery, testweightCalc, testforwardIndexer, testtitleIndexer, testbodyIndexer, p);
        r = new Retrieval(testcs);
        testss = new StopStem("stopwords.txt"); 
    }

    /** 
     * IDManager Test
     * Testing addWords() and getWordId() 
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testAddWordsAndGetWordID() throws RocksDBException
    {
        Vector<String> testing = new Vector<String>();
        testing.add("testing");
        testing.add("comp4321"); 
        testing.add("searchengines");
        testidManager.addWords(testing); 
        assertNotEquals(testidManager.getWordId("comp4321"), "");
    }

    /** 
     * IDManager Test
     * Testing addUrl() and getUrlId() 
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testAddUrl() throws RocksDBException
    {
        String sampleUrl = "www.google.com"; 
        testidManager.addUrl(sampleUrl);
        assertNotEquals(testidManager.getUrlId(sampleUrl), "");
    }


    /**
     * Indexer test
     * test the getByKey function
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testgetByKey() throws RocksDBException {
        String url = "www.google.com"; 
        testidManager.addUrl(url); 
        assertNotEquals(testpidIndexer.getByKey(url), ""); 
    }

    /**
     * Indexer test
     * delEntry test
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testdelEntry() throws RocksDBException {
        String url = "www.google.com";
        assertNotEquals(testpidIndexer.getByKey(url), ""); 
        testpidIndexer.delEntry(url);
        assertEquals(testpidIndexer.getByKey(url), ""); 
    }

    /**
     * Weight calc test
     * testing parseTF
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testparseTF() {
        String listOfTF = "doc1:10,doc2:15,doc3:20"; 
        HashMap<String, Integer> temp = testweightCalc.parseTF(listOfTF); 
        HashMap<String, Integer> expected = new HashMap<String, Integer>(); 
        expected.put("doc1", 10); 
        expected.put("doc2", 15);
        expected.put("doc3", 20); 
        assertEquals(expected, temp); 
    }

    /**
     * Weight calc test
     * testing parseWords
     * @throws RocksDBException rocks db exception 
     */
    @Test
    public void testparseWords() {
        String listOfWords = "hello,my,name,is,wilson"; 
        ArrayList<String> temp = testweightCalc.parseWords(listOfWords); 
        ArrayList<String> expected = new ArrayList<String>(); 
        expected.add("hello");
        expected.add("my");
        expected.add("name"); 
        expected.add("is"); 
        expected.add("wilson"); 
        assertEquals(expected, temp);
    }
    
    /**
     * Weight calc test
     * testing getDocFreq
     * @throws RocksDBException rocks db exception
     */
    @Test
    public void testgetDocFreq() throws RocksDBException {
        testidManager.addUrl("www.google.com"); 
        testbodyIndexer.addEntry("www.google.com", "wow", "12"); 
        testidManager.addUrl("www.yahoo.com");
        testbodyIndexer.addEntry("www.yahoo.com", "wow", "15"); 
        String wordID = testidManager.getWordId("wow"); 
        assertEquals(2, testweightCalc.getDocFreq(wordID));
    }
    
    /**
     * Parser test
     * extractWords body, title   test
     */
    @Test
    public void testExtractWords1() {
        String body = "i am a student at hkust"; 
        String title = "studying at"; 
        Vector<String> result = p.extractWords(body, title); 
        Vector<String> expected = new Vector<>(); 
        expected.add("studying"); 
        expected.add("at"); 
        expected.add("i");
        expected.add("am");
        expected.add("a"); 
        expected.add("student"); 
        expected.add("at"); 
        expected.add("hkust");
        assertEquals(expected, result); 
    }

    /**
     * Parser test
     * extractWords text test
     */
    @Test
    public void testExtractWords2() {
        String body = "i am a student at hkust"; 
        Vector<String> result = p.extractWords(body); 
        Vector<String> expected = new Vector<>(); 
        expected.add("i");
        expected.add("am");
        expected.add("a"); 
        expected.add("student"); 
        expected.add("at"); 
        expected.add("hkust");
        assertEquals(expected, result); 
    }

    /**
     * Parser test
     * getActualLink test
     */
    @Test
    public void testGetActualLink() {
        String url = "www.google.com/hkust#ahsuMuqn201Mi2n0?query";
        String actualurl = "www.google.com/hkust";
        assertEquals(p.getActualLink(url), actualurl); 
    }

    // /**
    //  * Cossim test
    //  * docs test
    //  */
    // @Test
    // public void testdocs() {
    //     String testString = "doc1#12 doc2#15 doc3#2";
    //     HashMap<String, String> expected = new HashMap<String, String>(); 
    //     expected.put("doc1", "12");
    //     expected.put("doc2", "15");
    //     expected.put("doc3", "2"); 
    //     HashMap<String, String> result = testcs.docs(testString); 
    //     assertEquals(expected, result);
    // }

    /**
     * Stopstem test
     * ss test
     */
    @Test
    public void testss() {
        HashMap<String, String> testing = new HashMap<String, String>();
        testing.put("computers", "5"); //lets say the wordposition is 5
        HashMap<String, String> result = testss.ss(testing); //outputs <"comput", "5">
        HashMap<String, String> expected = new HashMap<>(); 
        
        expected.put("comput", "5"); 
        assertEquals(expected, result);
    }

    /**
     * Stop Stem test
     * Parse query test
     */
    @Test
    public void testparseQuery() {
        String query = ".... \" .as \"    hbnong bowue           \"asda\" .q? "; 
        Vector<String> result = testss.parseQuery(query); 
        Vector<String> expected = new Vector<>(); 
        expected.add("as");
        expected.add("asda");
        expected.add("hbnong");
        expected.add("bowu");
        expected.add("q"); 
        assertEquals(expected, result); 
    }

}
