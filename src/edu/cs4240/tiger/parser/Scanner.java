package edu.cs4240.tiger.parser;

import java.io.InputStream;

/**
 * @author Roi Atalla
 */
public class Scanner {
	private InputStream source;
	
	public Scanner(InputStream source) {
		this.source = source;
	}
	
	public Token nextToken() {
		return null;
	}
}
