package edu.cs4240.tiger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.intermediate.TigerIRGenerator;
import edu.cs4240.tiger.intermediate.interpreter.TigerInterpreter;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.node.RuleNode;
import edu.cs4240.tiger.util.StringifyTigerIR;

/**
 * @author Roi Atalla
 */
public class Tiger {
	private static final String VERSION = "1.1";
	
	private static void printUsage() {
		System.out.println("Tiger Compiler v" + VERSION + " by Roi Atalla.\n");
		System.out.println("Usage: java -jar parser.jar [--tokens] [--ast] sourceFile.tgr");
		System.out.println("--tokens     prints a space-separated list of tokens");
		System.out.println("--ast        prints s-expression of the AST");
		System.out.println("--printil    prints the generated IR");
		System.out.println("--runil      runs the generated IR");
		System.out.println("--debug -d   prints out debug messages in interpreter");
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
		boolean printil = false;
		boolean runil = false;
		boolean debug = false;
		
		for(String s : args) {
			switch(s) {
				case "--tokens":
					printTokens = true;
					break;
				case "--ast":
					printAST = true;
					break;
				case "--printil":
					printil = true;
					break;
				case "--runil":
					runil = true;
					break;
				case "-d":
				case "--debug":
					debug = true;
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
		}
		catch(IOException exc) {
			System.err.println("Failed to open file " + source);
			return;
		}
		catch(TigerParseException exc) {
			System.err.println(exc.toString());
			return;
		}
		
		RuleNode ast;
		TigerAnalyzer analyzer;
		try {
			analyzer = new TigerAnalyzer(ast = parser.parse());
			analyzer.run();
		}
		catch(TigerParseException exc) {
			System.err.println(exc.toString());
			return;
		}
		
		if(printTokens) {
			for(TigerToken token : parser.getTokens()) {
				System.out.print(token.getTokenString() + " ");
			}
			System.out.println();
		}
		
		if(printAST) {
			System.out.println(ast);
		}
		
		TigerIRGenerator generator = new TigerIRGenerator(ast, analyzer.getSymbolTable());
		List<String> ir = generator.generateIR();
		
		if(printil) {
			System.out.println(StringifyTigerIR.stringifyIR(ir));
		}
		
		if(runil) {
			TigerInterpreter interpreter = new TigerInterpreter(ir);
			interpreter.run(debug);
		}
	}
}
