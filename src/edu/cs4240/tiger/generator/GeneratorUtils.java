package edu.cs4240.tiger.generator;

import java.util.Random;

import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class GeneratorUtils {
	public static String generateIntlit(Random rng) {
		return String.valueOf(rng.nextInt(1000));
	}
	
	public static String generateFloatlit(Random rng) {
		return String.format("%.3f", rng.nextDouble() * 1000);
	}
	
	private static char generateIDChar(Random rng) {
		switch(rng.nextInt(4)) {
			case 0:
				return (char)(rng.nextInt('z' - 'a') + 'a');
			case 1:
				return Character.toUpperCase((char)(rng.nextInt('z' - 'a') + 'a'));
			case 2:
				return (char)(rng.nextInt('9' - '0') + '0');
			default:
				return '_';
		}
	}
	
	public static String generateID(Random rng) {
		String id = "";
		
		if(rng.nextBoolean()) {
			id += "_";
			
			while(rng.nextBoolean())
				id += "_";
			
			char c;
			while((c = generateIDChar(rng)) == '_') ;
			
			id += c;
		} else {
			char c;
			while(Character.isDigit(c = generateIDChar(rng)) || c == '_') ;
			
			id += c;
		}
		
		do {
			int len = rng.nextInt(5) + 3;
			for(int i = 0; i < len; i++) {
				id += generateIDChar(rng);
			}
			
			try {
				if(id.equals("type")) {
					continue;
				}
				
				if(id.toUpperCase().equals("ID") || id.toUpperCase().equals("INTLIT") || id.toUpperCase().equals("FLOATLIT")) {
					break;
				}
				
				TigerTokenClass.valueOf(id.toUpperCase());
			}
			catch(Exception exc) {
				break;
			}
		} while(true);
		
		return id;
	}
}
