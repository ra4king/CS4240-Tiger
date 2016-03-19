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
	private static final String VERSION = "1.0.1";
	
	private static void printUsage() {
		System.out.println("Tiger Compiler v" + VERSION + " by Roi Atalla.\n");
		System.out.println("Usage: java -jar parser.jar [--tokens] [--ast] sourceFile.tgr");
		System.out.println("--tokens     prints a space-separated list of tokens");
		System.out.println("--ast        prints s-expression of the AST");
		System.out.println("--help  -h   prints this help message");
		System.out.println();
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
				case "-h":
				case "--help":
					printUsage();
					return;
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
		catch(IOException exc) {
			System.err.println("Failed to open file " + source);
			return;
		}
		catch(TigerParseException exc) {
			System.err.println(exc.toString());
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
				System.err.println(exc);
			}
		}
	}
}
