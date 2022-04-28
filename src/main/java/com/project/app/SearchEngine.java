package com.project.app;

public class SearchEngine {
  String query;

  public SearchEngine(String query){
    this.query = query;
  }

  public String search(){
    return this.query;
  }
}
