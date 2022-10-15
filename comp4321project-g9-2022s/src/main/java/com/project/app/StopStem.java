package com.project.app;

import java.io.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.*; 
import java.util.Vector; 

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
	 * @param str the string to stop stem
	 * @return the stem
	 */
	public String ss(String str){
		String stemmedString = "";
		
		if(str.length() > 0){
            str = str.trim().toLowerCase();
            
            String[] words = str.split(" ");
    
            for(String word: words){
                if(!stopWords.contains(word) && word != "")
                    stemmedString = stemmedString.concat(porter.stripAffixes(word) + " ");
            }
        }
		return stemmedString;
	}

	/**
	 * Custom stop stem for wordPosition HashMap
	 * @param wordPosition the hashmap of wordid, word positions
	 * @return a version of the hashmap with all the keys stemmed
	 */
	public HashMap<String, String> ss(HashMap<String, String> wordPosition)
	{
		HashMap<String, String> stemmedHashMap = new HashMap<String, String>();
		wordPosition.forEach((key, val) -> {
			String lowKey = key.toLowerCase();
			if(!stopWords.contains(lowKey) && lowKey.length() > 0){
				String stemmedKey = porter.stripAffixes(lowKey);
				if(stemmedHashMap.containsKey(stemmedKey)){
					String oldVal = stemmedHashMap.get(stemmedKey);
					stemmedHashMap.replace(stemmedKey, oldVal + "," + val);
				}
				else{
					stemmedHashMap.put(stemmedKey, val);
				}
			}
		});

		HashMap<String, String> stemmedHashMap2 = new HashMap<String, String>();
    stemmedHashMap.forEach((key, val) -> {
      if(key != "") stemmedHashMap2.put(key, val);
    });

		return stemmedHashMap2;
	}

	/**
	 * Method to parse the query into a vector string, supports phrase search
	 * @param query  the string to parse
	 * @return a vector of strings
	 */
	public Vector<String> parseQuery(String query) {
		Vector<String> result = new Vector<String>(); 
		Pattern p = Pattern.compile("\"([^\"]*)\""); 
		Matcher m = p.matcher(query); 
		String nonQuoted = query;

		while (m.find()) {    //Parse phrase
			String phrase = m.group(1);
			String [] temp = phrase.split("\\s+"); 
			String formattedPhrase = ""; 
			for (String s : temp) 
				formattedPhrase = formattedPhrase + " " + s; 
			formattedPhrase = ss(formattedPhrase).trim(); 
			if (!formattedPhrase.isEmpty())
				result.add(formattedPhrase); 
			nonQuoted = nonQuoted.replace("\"" + phrase + "\"", ""); 
		}

		String [] split = nonQuoted.trim().split("\\s+");    //Parse non-phrase
		for (String s : split) {
			String ssTerm = ss(s).trim(); 
			if (!ssTerm.isEmpty())
				result.add(ssTerm); 
		}

		return result; 
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
