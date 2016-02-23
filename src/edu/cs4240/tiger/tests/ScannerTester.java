package edu.cs4240.tiger.tests;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.cs4240.tiger.parser.Scanner;
import edu.cs4240.tiger.parser.Token;

/**
 * @author Roi Atalla
 */
public class ScannerTester {
	public static void main(String[] args) throws Exception {
		List<String> lines = Files.readAllLines(Paths.get(System.getProperty("user.dir"), "tests/count.tgr"));
		String source = "";
		for(String s : lines)
			source += s + '\n';
		
		Scanner scanner = new Scanner(source);
		
		Token token;
		while((token = scanner.nextToken()) != null) {
			System.out.println(token);
		}
	}
}
