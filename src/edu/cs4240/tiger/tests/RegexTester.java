package edu.cs4240.tiger.tests;

import edu.cs4240.tiger.util.Regex;
import edu.cs4240.tiger.util.Regex.Match;

/**
 * @author Roi Atalla
 */
public class RegexTester {
	public static void main(String[] args) {
		Regex regex = Regex.and(Regex.string("hello "), Regex.oneOrMore(Regex.or(Regex.letter(), Regex.number())), Regex.string("!"));
		
		System.out.println("Now testing regex: " + regex);
		
		printMatch(regex, "hello Roi!");
		printMatch(regex, "hello 581238casdf919f! hahahahaha");
		printMatch(regex, "Hello Roi!");
		printMatch(regex, "hello      !");
		printMatch(regex, "hello IamRoi !");
		
		// ([A-Za-z]|_+[A-Za-z0-9])[A-Za-z0-9_]*
		Regex id = Regex.and(Regex.or(Regex.letter(), 
		                              Regex.and(Regex.oneOrMore(Regex.string("_")), Regex.alphanumeric())),
		                     Regex.zeroOrMore(Regex.wordChar()));
		
		System.out.println("\nNow testing regex: " + id);
		
		printMatch(id, "myVar");
		printMatch(id, "0var");
		printMatch(id, "_a_a_a_aaaa____");
		printMatch(id, "____0asdasdf9128hjpvnxl___");
		printMatch(id, "myVar is the best");
		printMatch(id, "thisIsAnotherVariable1234567890_");
		printMatch(id, "a");
		printMatch(id, "_a");
		printMatch(id, "_0");
		printMatch(id, "__");
		
		// [0 - 9]+
		Regex intLit = Regex.oneOrMore(Regex.number());
		
		System.out.println("\nNow testing regex: " + intLit);
		
		printMatch(intLit, "01234");
		printMatch(intLit, "1");
		
		// [0-9]+\.[0-9]*
		Regex floatLit = Regex.and(Regex.oneOrMore(Regex.number()), Regex.string("."), Regex.zeroOrMore(Regex.number()));
		
		System.out.println("\nNow testing regex: " + floatLit);
		
		printMatch(floatLit, "1.2");
		printMatch(floatLit, "12345.67890");
		printMatch(floatLit, "1234.");
		printMatch(floatLit, ".1234");
	}
	
	public static void printMatch(Regex regex, String s) {
		Match match = regex.match(s);
		if(match != null)
			System.out.println(s + " matched: " + match.getMatch());
		else
			System.out.println(s + " did not match.");
	}
}
