package edu.cs4240.tiger.parser;

import edu.cs4240.tiger.regex.Regex.Match;

/**
 * @author Roi Atalla
 */
public class Scanner {
	private String source;
	private int i;
	
	private String currLine;
	private int currLineNum, currLineIdx;
	
	public Scanner(String source) {
		this.source = source;
		
		currLineIdx = source.indexOf('\n');
		if(currLineIdx == -1)
			currLineIdx = source.length();
		currLine = source.substring(0, currLineIdx).trim();
		currLineNum = 1;
	}
	
	private String buffer = "";
	
	public Token nextToken() {
		while(true) {
			boolean newLine = false;
			
			try {
				boolean matched = false;
				
				if(i < source.length()) {
					do {
						char c = source.charAt(i);
						newLine = c == '\n';
						
						buffer = ltrim(buffer + c);
						
						if(buffer.isEmpty()) i++;
						else break;
					} while(true);
					
					for(TokenClass tc : TokenClass.values()) {
						Match match = tc.regex.match(buffer);
						if(match != null && match.getMatch().length() == buffer.length()) {
							matched = true;
							break;
						}
					}
				}
				
				if(!matched) {
					if(buffer.length() == 0)
						return null;
					
					String backtrack = buffer.substring(0, buffer.length() - (i == source.length() ? 0 : 1));
					for(TokenClass tc : TokenClass.values()) {
						Match match = tc.regex.match(backtrack);
						if(match != null && match.getMatch().length() == backtrack.length()) {
							buffer = buffer.substring(backtrack.length());
							return new Token(tc, match.getMatch(), currLine, currLineNum);
						}
					}
					
					return null;
				}
			} finally {
				i++;
				
				if(newLine) {
					int prevLineIdx = currLineIdx;
					currLineIdx = source.indexOf('\n', prevLineIdx + 1);
					if(currLineIdx == -1)
						currLineIdx = source.length();
					currLine = source.substring(prevLineIdx, currLineIdx).trim();
					currLineNum++;
				}
			}
		}
	}
	
	private static String ltrim(String s) {
		for(int i = 0; i < s.length(); i++) {
			if(!Character.isWhitespace(s.charAt(i)))
				return s.substring(i);
		}
		
		return "";
	}
}
