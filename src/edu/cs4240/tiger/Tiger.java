package edu.cs4240.tiger;

/**
 * @author Roi Atalla
 */
public class Tiger {
	public static void main(String[] args) {
		if(args.length == 0) {
			printUsage();
			return;
		}
		
		System.out.println("Compiling " + args[0] + "...");
	}
	
	private static void printUsage() {
		System.out.println("Usage: tigerc sourceFile.tgr");
	}
}
