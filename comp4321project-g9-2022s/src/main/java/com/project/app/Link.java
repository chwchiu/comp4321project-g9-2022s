package com.project.app;

public class Link{
    String url;
    int level;
    int distance; 
    /**
     * Constructor for link class, class is just to help with searching according to depth
     * @param url the url of the page
     * @param level the depth of the page 
     */
    Link (String url, int level, int distance) {  
        this.url = url;
        this.level = level;
        this.distance = distance; 
    }  
}
