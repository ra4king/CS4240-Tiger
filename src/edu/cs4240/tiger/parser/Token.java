package edu.cs4240.tiger.parser;

/**
 * @author Roi Atalla
 */
public class Token {
	private TokenClass tokenClass;
	private String token;
	
	private String line;
	private int lineNum;
	
	public Token(TokenClass tokenClass, String token, String line, int lineNum) {
		this.tokenClass = tokenClass;
		this.token = token;
		this.line = line;
		this.lineNum = lineNum;
	}
}
