package edu.cs4240.tiger.parser;

/**
 * @author Roi Atalla
 */
public class TigerParseException extends Exception {
	private String line;
	private int lineNum, indexNum;
	
	public TigerParseException(String message) {
		this(message, null, -1, -1);
	}
	
	public TigerParseException(String message, TigerToken token) {
		this(message, token.getLine(), token.getLineNumber(), token.getIndexNumber());
	}
	
	public TigerParseException(String message, String line, int lineNum, int indexNum) {
		super(message);
		
		this.line = line;
		this.lineNum = lineNum;
		this.indexNum = indexNum;
	}
	
	public String getLine() {
		return line;
	}
	
	public int getLineNumber() {
		return lineNum;
	}
	
	public int getIndexNumber() {
		return indexNum;
	}
	
	@Override
	public String toString() {
		if(line == null)
			return getMessage();
		
		String s = getMessage() + " on line " + lineNum + ", index " + indexNum + ": ";
		int len = s.length() + indexNum;
		
		String trimmed = line.trim();
		len -= line.indexOf(trimmed);
		
		s += trimmed + "\n";
		
		for(int i = 0; i < len; i++) {
			s += " ";
		}
		s += "^";
		
		return s;
	}
}
