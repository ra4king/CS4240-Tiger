package edu.cs4240.tiger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Queue;

import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.TigerToken;

/**
 * @author Roi Atalla
 */
public class Tiger {
	private static void printUsage() {
		System.out.println("Usage: java -jar parser.jar [--tokens] [--ast] sourceFile.tgr");
	}
	
	public static void main(String[] args) {
		if(args.length == 0) {
			printUsage();
			return;
		}
		
		String source = null;
		boolean printTokens = false;
		boolean printAST = false;
		
		for(String s : args) {
			switch(s) {
				case "--tokens":
					printTokens = true;
					break;
				case "--ast":
					printAST = true;
					break;
				default:
					source = s;
					break;
			}
		}
		
		if(source == null || (!printTokens && !printAST)) {
			printUsage();
			return;
		}
		
		TigerParser parser;
		try {
			parser = new TigerParser(new TigerScanner(new BufferedReader(new FileReader(source))));
		}
		catch(IOException | TigerParseException exc) {
			exc.printStackTrace();
			return;
		}
		
		if(printTokens) {
			System.out.println("Tokens:");
			Queue<TigerToken> tokenQueue = parser.getTokenQueue();
			for(TigerToken token; tokenQueue.size() > 0;) {
				token = tokenQueue.remove();
				System.out.print(token.getToken() + " ");
			}
			System.out.println("\n");
		}
		
		if(printAST) {
			System.out.println("AST:");
			try {
				System.out.println(parser.parse());
			}
			catch(TigerParseException exc) {
				exc.printStackTrace();
			}
		}
	}
}
