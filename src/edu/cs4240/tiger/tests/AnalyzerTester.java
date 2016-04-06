package edu.cs4240.tiger.tests;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.node.RuleNode;

/**
 * @author Roi Atalla
 */
public class AnalyzerTester {
	public static void main(String[] args) throws Exception {
		String file = "test.tgr";
		try(BufferedReader reader = new BufferedReader(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "src/edu/cs4240/tiger/tests/" + file)))) {
			TigerParser parser = new TigerParser(new TigerScanner(reader));
			RuleNode ast = parser.parse();
			TigerAnalyzer analyzer = new TigerAnalyzer(ast);
			analyzer.run();
			System.out.println(file + " parsed and analyzed successfully!");
		} catch(TigerParseException exc) {
			System.err.println(exc.toString());
		}
	}
}
