package com.project.app;

public class SearchEngine {
  String query;

  //Constructor takes in a query
  public SearchEngine(String query){
    this.query = query;
  }

  //Return a string thats the result of the search
  //converts retrieval into string
  public String search(){
    return this.query;
  }
}
