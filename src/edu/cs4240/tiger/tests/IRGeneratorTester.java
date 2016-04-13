package edu.cs4240.tiger.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import edu.cs4240.tiger.analyzer.TigerAnalyzer;
import edu.cs4240.tiger.intermediate.TigerIRGenerator;
import edu.cs4240.tiger.intermediate.interpreter.TigerInterpreter;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.util.StringifyTigerIR;

/**
 * @author Roi Atalla
 */
public class IRGeneratorTester {
	public static void main(String[] args) throws IOException, TigerParseException {
		TigerParser parser = new TigerParser(new TigerScanner(Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), "src/edu/cs4240/tiger/tests/test_ir.tgr"))));
		TigerAnalyzer analyzer = new TigerAnalyzer(parser.parse());
		analyzer.run();
		
		TigerIRGenerator generator = new TigerIRGenerator(parser.parse(), analyzer.getSymbolTable());
		List<String> ir = generator.generateIR();
		
		System.out.println(StringifyTigerIR.stringifyIR(ir));
		TigerInterpreter interpreter = new TigerInterpreter(ir);
		interpreter.run(true);
	}
}
