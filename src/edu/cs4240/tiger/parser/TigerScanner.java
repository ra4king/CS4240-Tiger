package edu.cs4240.tiger.parser;

import java.io.BufferedReader;
import java.io.IOException;

import edu.cs4240.tiger.regex.Regex.Match;

/**
 * @author Roi Atalla
 */
public class TigerScanner {
	private BufferedReader source;
	
	// to keep track of the current line
	private String currLine, buffer;
	private int currLineNum;
	
	public TigerScanner(BufferedReader source) throws IOException {
		this.source = source;
		
		readNextLine();
	}
	
	private void readNextLine() throws IOException {
		String s;
		do {
			s = source.readLine();
		} while(s != null && s.trim().isEmpty());
		
		if(s == null) {
			buffer = currLine = null;
			source.close();
		} else {
			currLine = s; 
			buffer = s.trim();
			currLineNum++;
		}
	}
	
	public TigerToken nextToken() throws IOException {
		if(buffer == null || buffer.isEmpty())
			return null;
		
		Match bestMatch = null;
		TigerTokenClass bestMatchToken = null;
		for(TigerTokenClass tokenClass : TigerTokenClass.values()) {
			Match match = tokenClass.regex.match(buffer);
			if(match != null && (bestMatch == null || match.getMatch().length() > bestMatch.getMatch().length())) {
				bestMatch = match;
				bestMatchToken = tokenClass;
			}
		}
		
		if(bestMatch != null) {
			buffer = buffer.substring(bestMatch.getMatch().length()).trim();
			TigerToken token = new TigerToken(bestMatchToken, bestMatch.getMatch(), currLine, currLineNum);
			
			if(buffer.isEmpty()) {
				readNextLine();
			}
			
			return token;
		}
		
		throw new RuntimeException("Invalid token on line " + currLineNum + ", index " + currLine.lastIndexOf(buffer) + ": " + buffer);
	}
}
