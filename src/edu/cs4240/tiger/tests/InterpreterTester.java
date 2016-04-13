package edu.cs4240.tiger.tests;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import edu.cs4240.tiger.intermediate.interpreter.TigerInterpreter;

/**
 * @author Roi Atalla
 */
public class InterpreterTester {
	public static void main(String[] args) throws IOException {
		new TigerInterpreter(Files.readAllLines(Paths.get(System.getProperty("user.dir"), "src/edu/cs4240/tiger/tests/test.tir"))).run();
	}
}
