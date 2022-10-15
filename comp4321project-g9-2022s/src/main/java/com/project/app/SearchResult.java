package com.project.app;

import java.util.ArrayList;

public class SearchResult {
  private String score;
  private String title;
  private String lastMod;
  private String url;
  private ArrayList<String> children;
  private ArrayList<String> parents;

  public SearchResult(String score, String title, String lastMod, String url, ArrayList<String> children, ArrayList<String> parent){
    this.score = score;
    this.title = title;
    this.lastMod = lastMod;
    this.url = url;
    this.children = children;
    this.parents = parent;
  }

  public String getScore() {
    return this.score;
  }
  public String getTitle() {
    return this.title;
  }
  public String getLastMod() {
    return this.lastMod;
  }
  public String getUrl() {
    return this.url;
  }
  public ArrayList<String> getChildren() {
    return this.children;
  }
  public ArrayList<String> getParents() {
    return this.parents;
  }
}
