package com.project.app; 

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.rocksdb.RocksDBException;
import org.rocksdb.RocksIterator;

public class SearchEngine {
  String query;
  CosSim cossim;
  IDIndexer pidIndexer;
  IDIndexer widIndexer;
  IDManager idManager;
  InvertedIndexer bodyIndexer;
  InvertedIndexer titleIndexer;
  ForwardIndexer forwardIndexer;
  PagePropertiesIndexer ppIndexer;
  TFIndexer tfIndexer;
  WeightCalc weightCalc; 
  ChildIndexer ci;
  ParentIndexer pi;
  StopStem ss;
  HashMap<Integer, String> top50;
  Crawler c; 
  PagePropertiesIndexer pp;
  public int entries;

  public ArrayList<String> titles = new ArrayList<String>();
  public ArrayList<String> links = new ArrayList<String>();
  public ArrayList<String> body = new ArrayList<String>();

  //Constructor takes in a query
  public SearchEngine(String query){
    this.query = query;
    try{
      this.pidIndexer = new IDIndexer("/root/comp4321project-g9-2022s/db/PageIDIndex");
      this.widIndexer = new IDIndexer("/root/comp4321project-g9-2022s/db/WordIDIndex");
      this.idManager = new IDManager(this.pidIndexer, this.widIndexer);
      this.bodyIndexer = new InvertedIndexer("/root/comp4321project-g9-2022s/db/BodyIndex", idManager);
      this.titleIndexer = new InvertedIndexer("/root/comp4321project-g9-2022s/db/TitleIndex", idManager);
      this.forwardIndexer = new ForwardIndexer("/root/comp4321project-g9-2022s/db/ForwardIndex", idManager);
      this.ppIndexer = new PagePropertiesIndexer("/root/comp4321project-g9-2022s/db/PagePropertiesIndex", idManager);
      this.tfIndexer = new TFIndexer("/root/comp4321project-g9-2022s/db/TFIndex", idManager);
      this.weightCalc = new WeightCalc("/root/comp4321project-g9-2022s/db/WeightIndex", tfIndexer, forwardIndexer, titleIndexer, bodyIndexer, idManager); 
      this.ci = new ChildIndexer("/root/comp4321project-g9-2022s/db/ChildIndex", idManager);
      this.pi = new ParentIndexer("/root/comp4321project-g9-2022s/db/ParentIndex", idManager);
      this.ss = new StopStem("/root/comp4321project-g9-2022s/stopwords.txt");
      this.cossim = new CosSim("/root/comp4321project-g9-2022s/db/CosSimIndex",
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
  public String search(){
    String s = new String();

    RocksIterator iter = cossim.db.newIterator();
    RocksIterator iteration = pidIndexer.db.newIterator(); 
    RocksIterator iter2 = ppIndexer.db.newIterator();
    RocksIterator iterChild = ci.db.newIterator(); //key parent, val child
    RocksIterator iterParent = pi.db.newIterator(); //key child, val parent

    Retrieval r = new Retrieval(cossim);
    HashMap<Integer, String> top50 = r.top50();

    for (Map.Entry<Integer, String> entry : top50.entrySet()){
      String doc = entry.getValue();
      for(iter.seekToFirst(); iter.isValid(); iter.next()){
        String key = new String(iter.key()); 
        if (key.equals(doc)){
          String url = new String(); 
          for(iteration.seekToFirst(); iteration.isValid(); iteration.next()){
            String did = new String(iteration.value()); 
            did = "doc" + did; 
            if (did.equals(key)){
              url = new String (iteration.key());
              break; 
            }
          }

          //Connection conn = Jsoup.connect(url).fllowRedirects(false);
          //Response res; 

          String[] properties = new String[] {}; 
          for (iter2.seekToFirst(); iter2.isValid(); iter2.next()){
            String did = new String(iter2.key());
            did = "doc" + did; 
            if (did.equals(key)){
              String info = new String(iter2.value());
              properties = info.split(",");

            }
          }

          ArrayList<String> children = new ArrayList<>();
          for (iterChild.seekToFirst(); iterChild.isValid(); iterChild.next()){
            String did = new String(iterChild.key());
            did = "doc" + did;
            if (did.equals(key)){
              String value = new String(iterChild.value());
              String[] child = value.split(" ");
              for (int i =0; i < child.length; i++){
                String c_url = new String(); 
                for(iteration.seekToFirst(); iteration.isValid(); iteration.next()){
                  String c_did = new String(iteration.value()); 
                  c_did = "doc" + c_did; 
                  if (c_did.equals(key)){
                    c_url = new String (iteration.key());
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
          for (iterParent.seekToFirst(); iterParent.isValid(); iterParent.next()){
            String did = new String(iterParent.key());
            did = "doc" + did; 
            if (did.equals(key)){
              String value = new String(iterParent.value());
              String[] parentSplit = value.split(" ");
              for (int i =0; i < parentSplit.length; i++){
                String p_url = new String();
                String p_did = new String();
                for(iteration.seekToFirst(); iteration.isValid(); iteration.next()){
                  p_did = new String(iteration.value()); 
                  p_did = "doc" + p_did;
                }
                if (p_did.equals(key)){
                  p_url = new String (iteration.key());
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

          String score = new String(iter.value()); 
          Double df = Double.parseDouble(score);
          String score_f = String.format("%.6f", df);
          s = s + score_f + "\t" + properties[0] + "\n" + "\t" + "\t" + url + "\n" + "\t" + "\t" + properties[1] + "\n" + "\t" + "\t" + childrenString + "\n" + "\t" + "\t" + parentString + "\n";
          //s = s + entry.getKey() + ";" + entry.getValue() + ":" + new String(iter.value()) + " ";
        }
      }
    }
    return s;
  }
}

