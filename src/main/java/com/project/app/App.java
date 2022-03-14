package com.project.app;

public class App 
{
    public static void main( String[] args )
    {
        String url = "https://cse.hkust.edu.hk/";
        Crawler crawler = new Crawler(url);
        crawler.crawlLoop();
        System.out.println("\nSuccessfully Returned");
    }
}
