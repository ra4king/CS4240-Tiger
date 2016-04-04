package edu.cs4240.tiger.generator;

import java.util.Random;

import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.util.Utils;

/**
 * @author Roi Atalla
 */
public class TigerSourceGenerator {
	private static final String VERSION = "1.1.2";
	
	private static void printUsage() {
		System.out.println("Tiger Source Generator v" + VERSION + " by Roi Atalla\n");
		System.out.println("Usage:");
		System.out.println("-h,   --help      Prints this message.");
		System.out.println("-c    --correct   Generates semantically correct code. By default generates absolute random code.");
		System.out.println("-d N, --depth N   Set maximum rule depth. N must be a positive integer.");
		System.out.println("                  If not specified, one is chosen randomly in range [5,25).");
		System.out.println("-s L, --seed L    Set RNG seed value. L can be any long.");
		System.out.println("                  If not specified, the current nano time is used.");
		System.out.println("-v,   --verbose   Prints depth, seed, and time spent to generate program.");
	}
	
	public static void main(String[] args) {
		int depth = -1;
		long seed = System.nanoTime();
		boolean correct = false;
		boolean verbose = false;
		
		Random rng = null;
		
		try {
			for(int i = 0; i < args.length; i++) {
				switch(args[i]) {
					case "-h":
					case "--help":
						printUsage();
						return;
					case "-c":
					case "--correct":
						correct = true;
						break;
					case "-d":
					case "--depth":
						if(++i == args.length || depth != -1) {
							printUsage();
							return;
						}
						
						depth = Integer.parseInt(args[i]);
						if(depth <= 0) {
							printUsage();
							return;
						}
						
						break;
					case "-s":
					case "--seed":
						if(++i == args.length || rng != null) {
							printUsage();
							return;
						}
						
						seed = Long.parseLong(args[i]);
						rng = new Random(seed);
						break;
					case "-v":
					case "--verbose":
						verbose = true;
						break;
					default:
						printUsage();
						return;
				}
			}
		}
		catch(Exception exc) {
			printUsage();
			return;
		}
		
		if(rng == null) {
			rng = new Random(seed);
		}
		
		if(depth == -1) {
			depth = rng.nextInt(10) + 5;
		}
		
		long before = System.nanoTime();
		Node program;
		if(correct) {
			program = TigerSemanticallyCorrectGenerator.generate(rng, depth);
		} else {
			program = TigerRandomGenerator.generate(rng, depth);
		}
		long time = System.nanoTime() - before;
		
		if(verbose) {
			System.out.println("// Tiger Source Generator v" + VERSION + " by Roi Atalla");
			System.out.printf("// Generated Tiger program with depth=%d and seed=%d in %.3f ms\n\n", depth, seed, time / 1e6);
		}
		
		System.out.println(Utils.stringify(0, program));
	}
}
