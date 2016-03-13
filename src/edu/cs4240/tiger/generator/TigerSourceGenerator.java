package edu.cs4240.tiger.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Random;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerSymbol;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.util.Utils;

/**
 * @author Roi Atalla
 */
public class TigerSourceGenerator {
	private static final String version = "1.0.2";
	
	private static void printUsage() {
		System.out.println("Tiger Source Generator " + version + " by Roi Atalla\n");
		System.out.println("Usage:");
		System.out.println("-h,   --help      Prints this message.");
		System.out.println("-d N, --depth N   Set maximum rule depth. N must be a positive integer.");
		System.out.println("                  If not specified, one is chosen randomly in range [5,25).");
		System.out.println("-s L, --seed L    Set RNG seed value. L can be any long.");
		System.out.println("                  If not specified, the current nano time is used.");
		System.out.println("-v,   --verbose   Prints depth, seed, and time spent to generate program.");
	}
	
	public static void main(String[] args) {
		int depth = -1;
		long seed = System.nanoTime();
		boolean verbose = false;
		
		Random rng = null;
		
		try {
			for(int i = 0; i < args.length; i++) {
				switch(args[i]) {
					case "-h":
					case "--help":
						printUsage();
						return;
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
			depth = rng.nextInt(20) + 5;
		}
		
		Deque<TigerSymbol> symbolStack = new ArrayDeque<>();
		symbolStack.add(TigerProductionRule.PROGRAM);
		
		long before = System.nanoTime();
		Node program = generate(rng, depth, symbolStack);
		long time = System.nanoTime() - before;
		
		if(verbose) {
			System.out.println("// Tiger Source Generator " + version + " by Roi Atalla");
			System.out.printf("// Generated Tiger program with depth=%d and seed=%d in %.3f ms\n\n", depth, seed, time / 1e6);
		}
		
		System.out.println(Utils.stringify(0, program));
	}
	
	public static Node generate(Random rng, int limit, Deque<TigerSymbol> symbolStack) {
		TigerSymbol symbol = symbolStack.pop();
		
		if(symbol instanceof TigerProductionRule) {
			TigerProductionRule rule = (TigerProductionRule)symbol;
			
			while(true) {
				try {
					List<TigerSymbol> production = null;
					
					if(limit <= 0) {
						for(List<TigerSymbol> p : rule.productions) {
							if(p.get(0) == TigerTokenClass.EPSILON) {
								production = p;
								break;
							}
						}
					}
					
					if(production == null) {
						int trycount = 3;
						while(trycount-- > 0) {
							int idx = rng.nextInt(rule.productions.size());
							production = rule.productions.get(idx);
							
							if(production.get(0) != TigerTokenClass.EPSILON) {
								break;
							}
						}
					}
					
					RuleNode node = new RuleNode(rule);
					
					Deque<TigerSymbol> newSymbolStack = new ArrayDeque<>();
					for(int i = production.size() - 1; i >= 0; i--) {
						newSymbolStack.push(production.get(i));
					}
					
					while(!newSymbolStack.isEmpty()) {
						node.getChildren().add(generate(rng, limit - 1, newSymbolStack));
					}
					
					return node;
				}
				catch(Throwable t) {
				}
			}
		} else {
			return new LeafNode(new TigerToken((TigerTokenClass)symbol, generateTokenString(rng, (TigerTokenClass)symbol), "", 0, 0));
		}
	}
	
	private static String generateTokenString(Random rng, TigerTokenClass symbol) {
		if(symbol == TigerTokenClass.ID) {
			return generateID(rng);
		}
		
		if(symbol == TigerTokenClass.INTLIT) {
			return String.valueOf(rng.nextInt(1000));
		}
		
		if(symbol == TigerTokenClass.FLOATLIT) {
			return String.format("%.3f", rng.nextDouble() * 1000);
		}
		
		String token;
		
		token = Utils.specialTokenClassesToString.get(symbol);
		if(token != null) {
			return token;
		}
		
		return symbol.toString().replace("_", "").toLowerCase();
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
	
	private static String generateID(Random rng) {
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
