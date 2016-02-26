package edu.cs4240.tiger.parser;

/**
 * @author Roi Atalla
 */
public class TigerToken {
	private TigerTokenClass tokenClass;
	private String token;
	
	private String line;
	private int lineNum, indexNum;
	
	public TigerToken(TigerTokenClass tokenClass, String token, String line, int lineNum, int indexNum) {
		this.tokenClass = tokenClass;
		this.token = token;
		this.line = line;
		this.lineNum = lineNum;
		this.indexNum = indexNum;
	}
	
	public TigerTokenClass getTokenClass() {
		return tokenClass;
	}
	
	public void setTokenClass(TigerTokenClass tokenClass) {
		this.tokenClass = tokenClass;
	}
	
	public String getToken() {
		return token;
	}
	
	public void setToken(String token) {
		this.token = token;
	}
	
	public String getLine() {
		return line;
	}
	
	public void setLine(String line) {
		this.line = line;
	}
	
	public int getLineNumber() {
		return lineNum;
	}
	
	public void setLineNumber(int lineNum) {
		this.lineNum = lineNum;
	}
	
	public int getIndexNumber() {
		return indexNum;
	}
	
	public void setIndexNum(int indexNum) {
		this.indexNum = indexNum;
	}
	
	@Override
	public String toString() {
		return tokenClass + " - '" + token + "' on line " + lineNum + ", index " + indexNum + ": " + line;
	}
}
