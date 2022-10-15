package com.project.app; 

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class SearchEngine {
  public String query;
  public CosSim cossim;
  public IDIndexer pidIndexer;
  public IDIndexer widIndexer;
  public IDManager idManager;
  public InvertedIndexer bodyIndexer;
  public InvertedIndexer titleIndexer;
  public ForwardIndexer forwardIndexer;
  public PagePropertiesIndexer ppIndexer;
  public TFIndexer tfIndexer;
  public WeightCalc weightCalc; 
  public ChildIndexer ci;
  public ParentIndexer pi;
  public StopStem ss;
  public HashMap<Integer, String> top50;
  public Crawler c; 
  public PagePropertiesIndexer pp;
  public int entries;

  public ArrayList<String> titles = new ArrayList<String>();
  public ArrayList<String> links = new ArrayList<String>();
  public ArrayList<String> body = new ArrayList<String>();

  //Constructor takes in a query
  public SearchEngine(String query){
    this.query = query;
    try{
      this.pidIndexer = new IDIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\PageIDIndex");
      this.widIndexer = new IDIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\WordIDIndex");
      this.idManager = new IDManager(this.pidIndexer, this.widIndexer);
      this.bodyIndexer = new InvertedIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\BodyIndex", idManager);
      this.titleIndexer = new InvertedIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\TitleIndex", idManager);
      this.forwardIndexer = new ForwardIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\ForwardIndex", idManager);
      this.ppIndexer = new PagePropertiesIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\PagePropertiesIndex", idManager);
      this.tfIndexer = new TFIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\TFIndex", idManager);
      this.weightCalc = new WeightCalc("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\WeightIndex", tfIndexer, forwardIndexer, titleIndexer, bodyIndexer, idManager); 
      this.ci = new ChildIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\ChildIndex", idManager);
      this.pi = new ParentIndexer("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\ParentIndex", idManager);
      this.ss = new StopStem("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\stopwords.txt");
      this.cossim = new CosSim("C:\\Program Files\\Apache Software Foundation\\comp4321project-g9-2022s\\db\\CosSimIndex",
        idManager,
        query,
        weightCalc,
        forwardIndexer,
        titleIndexer,
        bodyIndexer,
        ss
        );
        this.cossim.calc();
    } catch(RocksDBException e){
      e.printStackTrace();
    }
  }

  //Return a string thats the result of the search
  //converts retrieval into string
  public ArrayList<SearchResult> search(){
    ArrayList<SearchResult> s = new ArrayList<SearchResult>();

    RocksIterator pageIter = this.pidIndexer.db.newIterator(); 
    RocksIterator ppIter = this.ppIndexer.db.newIterator();
    RocksIterator childIter = this.ci.db.newIterator(); //key parent, val child
    RocksIterator parentIter = this.pi.db.newIterator(); //key child, val parent
    RocksIterator cosIter = this.cossim.db.newIterator();

    Retrieval r = new Retrieval(cossim);
    HashMap<Integer, String> top50 = r.top50();

    for (Map.Entry<Integer, String> entry : top50.entrySet()){
      String doc = entry.getValue();
      for(cosIter.seekToFirst(); cosIter.isValid(); cosIter.next()){
        String key = new String(cosIter.key()); 
        if (key.equals(doc)){
          String url = new String(); 
          for(pageIter.seekToFirst(); pageIter.isValid(); pageIter.next()){
            String did = new String(pageIter.value()); 
            did = "doc" + did; 
            if (did.equals(key)){
              url = new String (pageIter.key());
              break; 
            }
          }

          String[] properties = new String[] {};
          String title = "";
          String lastMod = "";
          for (ppIter.seekToFirst(); ppIter.isValid(); ppIter.next()){
            String id = new String(ppIter.key());
            String docID = "doc" + id; 
            if (docID.equals(key)){
              String info = new String(ppIter.value());
              properties = info.split(",Last Modified");
              title = properties[0].replace("Title: ", "");
              lastMod = properties[1].split(",Size of Doc")[0];
            }
          }

          ArrayList<String> children = new ArrayList<>();
          for (childIter.seekToFirst(); childIter.isValid(); childIter.next()){
            String id = new String(childIter.key());
            String docID = "doc" + id;
            if (docID.equals(key)){
              String value = new String(childIter.value());
              String[] child = value.split(" ");
              for (int i =0; i < child.length; i++){
                String c_url = new String(); 
                for(pageIter.seekToFirst(); pageIter.isValid(); pageIter.next()){
                  String c_did = new String(pageIter.value());
                  c_did = "doc" + c_did; 
                  if (c_did.equals(key)){
                    c_url = new String (pageIter.key());
                  }
                }
                children.add(c_url);
              }
            }
          }
          String childrenString = "";
          for(String child: children){
            childrenString = childrenString + child + "\n" + "\t" + "\t";
          }

          ArrayList<String> parents = new ArrayList<>();
          for (parentIter.seekToFirst(); parentIter.isValid(); parentIter.next()){
            String did = new String(parentIter.key());
            did = "doc" + did; 
            if (did.equals(key)){
              String value = new String(parentIter.value());
              String[] parentSplit = value.split(" ");
              for (int i =0; i < parentSplit.length; i++){
                String p_url = new String();
                String p_did = new String();
                for(pageIter.seekToFirst(); pageIter.isValid(); pageIter.next()){
                  p_did = new String(pageIter.value()); 
                  p_did = "doc" + p_did;
                }
                if (p_did.equals(key)){
                  p_url = new String (pageIter.key());
                  break; 
                }
                parents.add(p_url);
              }
            }
          }
          String parentString = "";
          for(String parent: parents){
            parentString = parentString + parent + "\n" + "\t" + "\t";
          }

          String score = new String(cosIter.value()); 
          Double df = Double.parseDouble(score);
          String score_f = String.format("%.6f", df);

          s.add(new SearchResult(score_f, title, lastMod, url, children, parents));
        }
      }
    }
    return s;
  }

  @Override
  protected void finalize() {
    pidIndexer.db.close();
    widIndexer.db.close();
    bodyIndexer.db.close();
    titleIndexer.db.close();
    forwardIndexer.db.close();
    ppIndexer.db.close();
    tfIndexer.db.close();
    weightCalc.db.close();
    ci.db.close();
    pi.db.close();
    cossim.db.close();
  }
}

