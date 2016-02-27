package edu.cs4240.tiger.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;

/**
 * @author Roi Atalla
 */
public class ParserTester {
	public static void main(String[] args) throws IOException, TigerParseException {
		TigerParser parser = new TigerParser(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "tests/factorial.tgr")));
	}
}
