package edu.cs4240.tiger.tests;

import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerScanner;

/**
 * @author Roi Atalla
 */
public class AnalyzerTester {
	public static void main(String[] args) throws Exception {
		TigerParser parser = new TigerParser(new TigerScanner(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "tests/factorial.tgr"))));
		TigerAnalyzer analyzer = new TigerAnalyzer((RuleNode)parser.parse());
	}
}
