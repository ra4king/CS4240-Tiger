package edu.cs4240.tiger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerScanner;
import edu.cs4240.tiger.parser.TigerToken;

/**
 * @author Roi Atalla
 */
public class Tiger {
	public static void main(String[] args) {
		if(args.length == 0) {
			printUsage();
			return;
		}
		
		String source = null;
		boolean tokensOnly = false;
		
		for(int i = 0; i < args.length; i++) {
			if(args[i].equals("--token")) {
				tokensOnly = true;
			} else {
				source = args[i];
			}
		}
		
		if(source == null) {
			printUsage();
			return;
		}
		
		if(tokensOnly) {
			TigerScanner scanner;
			try {
				scanner = new TigerScanner(new BufferedReader(new FileReader(source)));
				
				TigerToken token;
				while((token = scanner.nextToken()) != null) {
					System.out.print(token.getToken() + " ");
				}
				System.out.println();
			}
			catch(IOException exc) {
				exc.printStackTrace();
				System.out.println("Unable to open " + source);
			}
			catch(TigerParseException exc) {
				exc.printStackTrace();
			}
		} else {
			System.out.println("Unimplemented...");
		}
	}
	
	private static void printUsage() {
		System.out.println("Usage: tigerc sourceFile.tgr");
	}
}
