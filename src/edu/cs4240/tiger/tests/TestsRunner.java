package edu.cs4240.tiger.tests;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.node.RuleNode;

/**
 * @author Roi Atalla
 */
public class TestsRunner {
	public static void main(String[] args) throws IOException, TigerParseException {
		File testsFolder = Paths.get(System.getProperty("user.dir"), "tests/").toFile();
		if(!testsFolder.isDirectory()) {
			System.out.println("Not a directory: " + testsFolder);
			return;
		}
		
		for(File file : testsFolder.listFiles()) {
			if(file.getName().endsWith("tgr")) {
				testFile(file);
				System.out.println();
			}
		}
	}
	
	private static void testFile(File file) {
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "tests/" + file.getName()))) {
			TigerParser parser = new TigerParser(new TigerScanner(reader));
			RuleNode astNode = parser.parse();
			String ast = astNode.toString();
			
			List<String> lines = Files.readAllLines(Paths.get(System.getProperty("user.dir"), "tests/" + file.getName().substring(0, file.getName().lastIndexOf('.')) + ".ast"));
			String line = "";
			for(String s : lines)
				line += s;
			
			if(ast.toLowerCase().equals(line)) {
				System.out.println(file.getName() + " matches!");
			} else {
				System.out.println(file.getName() + " NO MATCH!\n" + ast.toLowerCase());
			}
			
			TigerAnalyzer analyzer = new TigerAnalyzer(astNode);
			analyzer.run();
			
			System.out.println(file.getName() + " is semantically correct!");
		}
		catch(Exception exc) {
			System.err.println(file.getName() + " error:");
			exc.printStackTrace();
		}
	}
}
