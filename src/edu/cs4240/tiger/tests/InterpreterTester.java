package edu.cs4240.tiger.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import edu.cs4240.tiger.intermediate.interpreter.TigerInterpreter;

/**
 * @author Roi Atalla
 */
public class InterpreterTester {
	public static void main(String[] args) throws IOException {
		if(args.length == 0) {
			System.out.println("No file specified.");
			return;
		}
		
		ArrayList<String> input = new ArrayList<>();
		try(BufferedReader reader = Files.newBufferedReader(Paths.get(System.getProperty("user.dir"), args[0]))) {
			String s;
			while((s = reader.readLine()) != null) {
				input.add(s);
			}
		}
		
		new TigerInterpreter(input).run();
	}
}
