package edu.cs4240.tiger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
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
		
		if(source == null) {
			printUsage();
			return;
		}
		
		TigerParser parser;
		try {
			parser = new TigerParser(new TigerScanner(new BufferedReader(new FileReader(source))));
			
			TigerAnalyzer analyzer = new TigerAnalyzer(parser.parse());
			analyzer.run();
		}
		catch(IOException | TigerParseException exc) {
			System.err.println("\n" + exc);
			return;
		}
		
		if(printTokens) {
			for(TigerToken token : parser.getTokens()) {
				System.out.print(token.getToken() + " ");
			}
			System.out.println();
		}
		
		if(printAST) {
			try {
				System.out.println(parser.parse());
			}
			catch(TigerParseException exc) {
				System.err.println("\n" + exc);
			}
		}
	}
}
