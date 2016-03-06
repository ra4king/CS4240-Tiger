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
		String s = "";
		int beginComment = -1;
		do {
			String n = source.readLine();
			if(n != null) {
				s += n;
			} else if(beginComment == -1) {
				break;
			}
			
			currLineNum++;
			
			while(true) {
				beginComment = s.indexOf("//");
				if(beginComment != -1) {
					s = s.substring(0, beginComment);
				}
				
				beginComment = s.indexOf("/*");
				if(beginComment == -1) {
					break;
				}
				
				int endComment = s.indexOf("*/", beginComment);
				if(endComment == -1 && n == null) {
					endComment = s.length();
				}
				
				if(endComment == -1) {
					s = s.substring(0, beginComment + 2);
					break;
				} else {
					s = s.substring(0, beginComment) + " " + s.substring(endComment + 2);
				}
			}
		} while(beginComment > -1 || s.trim().isEmpty());
		
		if(s.trim().isEmpty()) {
			buffer = currLine = null;
			source.close();
		} else {
			currLine = s;
			buffer = s.trim();
		}
	}
	
	public TigerToken nextToken() throws IOException, TigerParseException {
		if(buffer == null || buffer.isEmpty()) {
			return null;
		}
		
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
