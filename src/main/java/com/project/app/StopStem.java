package com.project.app;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StopStem {
	private Porter porter;
	private java.util.HashSet<String> stopWords;
	public boolean isStopWord(String str) {
		return stopWords.contains(str);	
	}

	public StopStem(String str) {
		super();
		porter = new Porter();
		stopWords = new java.util.HashSet<String>();
		BufferedReader br = null;
		
		try{
			br = new BufferedReader(new FileReader(str));
			String line = br.readLine();
			while(line != null){
				stopWords.add(line);
				line = br.readLine();
			}
		}
		catch(IOException ioe){
			ioe.printStackTrace();
		}
		finally{
			try{
				if(br != null) br.close();
			}
			catch(IOException ioe){
				System.out.println("Error closing BufferedReader");
			}
		}
	}

	/**
	 * main method for stopstemming
	 * @param str
	 * @return
	 */
	public String ss(String str){
		String stemmedString = "";
		
		str = str.trim().toLowerCase();
		
		String[] words = str.split(" ");

		for(String word: words){
			if(!stopWords.contains(word) && word != "")
				stemmedString = stemmedString.concat(porter.stripAffixes(word) + " ");
		}

		return stemmedString;
	}

	public static void main(String[] arg) {
		StopStem stopStem = new StopStem("stopwords.txt");
		String input="";
		try{
			do {
				System.out.print("Please enter a single English word: ");
				BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
				input = in.readLine();
				if(input.length() > 0)
				{	
					if (stopStem.isStopWord(input))
						System.out.println("It should be stopped");
					else
			   			System.out.println("The stem of it is \"" + stopStem.ss(input)+"\"");
				}
			}
			while(input.length() > 0);
		}
		catch(IOException ioe) {
			System.err.println(ioe.toString());
		}
	}
}
