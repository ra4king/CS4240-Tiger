package edu.cs4240.tiger.tests;

import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerScanner;

/**
 * @author Roi Atalla
 */
public class AnalyzerTester {
	public static void main(String[] args) throws Exception {
		try {
			TigerParser parser = new TigerParser(new TigerScanner(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "tests/aaa.tgr"))));
			RuleNode ast = (RuleNode)parser.parse();
			System.out.println(ast);
			TigerAnalyzer analyzer = new TigerAnalyzer(ast);
			analyzer.run();
		} catch(TigerParseException exc) {
			System.err.println(exc.toString());
		}
	}
}
