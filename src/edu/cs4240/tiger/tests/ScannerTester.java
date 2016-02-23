package edu.cs4240.tiger.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.TigerToken;

/**
 * @author Roi Atalla
 */
public class ScannerTester {
	public static void main(String[] args) throws IOException {
		TigerScanner scanner = new TigerScanner(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "tests/count.tgr")));
		
		TigerToken token;
		while((token = scanner.nextToken()) != null) {
			System.out.println(token);
		}
	}
}
