package edu.cs4240.tiger.parser;

import java.io.BufferedReader;
import java.io.IOException;

import edu.cs4240.tiger.util.Regex.Match;

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
			s = clean(s);
			
			currLine = s; 
			buffer = s.trim();
			currLineNum++;
		}
	}
	
	private String clean(String s) {
		int beginComment = s.indexOf("/*");
		if(beginComment == -1)
			return s;
		
		int endComment = s.indexOf("*/");
		if(endComment == -1)
			return s.substring(0, beginComment).trim();
		
		return clean(s.substring(0, beginComment).trim() + " " + s.substring(endComment + 2).trim());
	}
	
	public TigerToken nextToken() throws IOException, TigerParseException {
		if(buffer == null || buffer.isEmpty())
			return null;
		
		Match bestMatch = null;
		TigerTokenClass bestMatchToken = null;
		for(TigerTokenClass tokenClass : TigerTokenClass.values()) {
			Match match = tokenClass.regex.match(buffer);
			if(match != null && match.getMatch().length() > 0 && (bestMatch == null || match.getMatch().length() > bestMatch.getMatch().length())) {
				bestMatch = match;
				bestMatchToken = tokenClass;
			}
		}
		
		if(bestMatch != null) {
			TigerToken token = new TigerToken(bestMatchToken, bestMatch.getMatch(), currLine, currLineNum, currLine.lastIndexOf(buffer));
			
			buffer = buffer.substring(bestMatch.getMatch().length()).trim();
			if(buffer.isEmpty()) {
				readNextLine();
			}
			
			return token;
		}
		
		throw new TigerParseException("Invalid token", buffer, currLineNum, currLine.lastIndexOf(buffer));
	}
}
