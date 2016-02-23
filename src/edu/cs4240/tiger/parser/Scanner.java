package edu.cs4240.tiger.parser;

import edu.cs4240.tiger.regex.Regex.Match;

/**
 * @author Roi Atalla
 */
public class Scanner {
	private String source;
	private int i;
	
	// to keep track of the current line
	private String currLine;
	private int currLineNum, currLineIdx;
	
	public Scanner(String source) {
		this.source = source;
		
		currLineIdx = source.indexOf('\n');
		if(currLineIdx == -1) {
			currLineIdx = source.length();
		}
		currLine = source.substring(0, currLineIdx).trim();
		currLineNum = 1;
	}
	
	private String buffer = "";
	
	public Token nextToken() {
		while(true) {
			boolean newLine = false;
			
			try {
				boolean matched = false;
				
				// Keep reading characters until the buffer is not an empty string
				while(i < source.length()) {
					char c = source.charAt(i);
					newLine = c == '\n';
					
					// if the buffer is empty and we are adding more emptiness, ignore
					if(buffer.isEmpty() && Character.isWhitespace(c)) {
						i++;
					} else {
						buffer += c;
						break;
					}
				}
				
				if(!buffer.isEmpty()) {
					for(TokenClass tc : TokenClass.values()) {
						Match match = tc.regex.match(buffer);
						if(match != null && match.getMatch().length() == buffer.length()) {
							matched = true; // we don't care about the match itself, just that something did match
							break;
						}
					}
				}
				
				// if nothing matched, backtrack!
				if(!matched) {
					if(buffer.length() == 0) {
						return null;
					}
					
					String backtrack = buffer.substring(0, buffer.length() - (i == source.length() ? 0 : 1));
					for(TokenClass tc : TokenClass.values()) {
						Match match = tc.regex.match(backtrack);
						if(match != null && match.getMatch().length() == backtrack.length()) {
							buffer = buffer.substring(backtrack.length()).trim();
							return new Token(tc, match.getMatch(), currLine, currLineNum);
						}
					}
					
					return null;
				}
			}
			finally {
				// incrementing the current index and tracking the current line must be done last
				
				i++;
				
				if(newLine) {
					int prevLineIdx = currLineIdx;
					currLineIdx = source.indexOf('\n', prevLineIdx + 1);
					if(currLineIdx == -1) {
						currLineIdx = source.length();
					}
					currLine = source.substring(prevLineIdx, currLineIdx).trim();
					currLineNum++;
				}
			}
		}
	}
}
