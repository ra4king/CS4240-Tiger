package edu.cs4240.tiger.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerSymbol;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerSourceGenerator {
	private static final String version = "1.0.1";
	
	private static Random rng;
	private static HashMap<TigerTokenClass, String> specialTokenClasses;
	
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
		} catch(Exception exc) {
			printUsage();
			return;
		}
		
		if(rng == null)
			rng = new Random(seed);
		
		if(depth == -1) {
			depth = (int)(rng.nextDouble() * 20 + 5);
		}
		
		specialTokenClasses = new HashMap<>();
		specialTokenClasses.put(TigerTokenClass.EPSILON, "ϵ");
		specialTokenClasses.put(TigerTokenClass.COMMA, ",");
		specialTokenClasses.put(TigerTokenClass.COLON, ":");
		specialTokenClasses.put(TigerTokenClass.SEMICOLON, ";");
		specialTokenClasses.put(TigerTokenClass.LPAREN, "(");
		specialTokenClasses.put(TigerTokenClass.RPAREN, ")");
		specialTokenClasses.put(TigerTokenClass.LBRACKET, "[");
		specialTokenClasses.put(TigerTokenClass.RBRACKET, "]");
		specialTokenClasses.put(TigerTokenClass.DOT, ".");
		specialTokenClasses.put(TigerTokenClass.PLUS, "+");
		specialTokenClasses.put(TigerTokenClass.MINUS, "-");
		specialTokenClasses.put(TigerTokenClass.MULT, "*");
		specialTokenClasses.put(TigerTokenClass.DIV, "/");
		specialTokenClasses.put(TigerTokenClass.EQUAL, "=");
		specialTokenClasses.put(TigerTokenClass.NOTEQUAL, "<>");
		specialTokenClasses.put(TigerTokenClass.LT, "<");
		specialTokenClasses.put(TigerTokenClass.GT, ">");
		specialTokenClasses.put(TigerTokenClass.LEQUAL, "<=");
		specialTokenClasses.put(TigerTokenClass.GEQUAL, ">=");
		specialTokenClasses.put(TigerTokenClass.AMP, "&");
		specialTokenClasses.put(TigerTokenClass.PIPE, "|");
		specialTokenClasses.put(TigerTokenClass.ASSIGN, ":=");
		
		Deque<TigerSymbol> symbolStack = new ArrayDeque<>();
		symbolStack.add(TigerProductionRule.PROGRAM);
		
		long before = System.nanoTime();
		Node program = generate(depth, symbolStack);
		long time = System.nanoTime() - before;
		
		if(verbose) {
			System.out.println("// Tiger Source Generator " + version + " by Roi Atalla");
			System.out.printf("// Generated Tiger program with depth=%d and seed=%d in %.3f ms\n\n", depth, seed, time / 1e6);
		}
		
		print(0, program);
		System.out.println();
	}
	
	private static void print(int level, Node node) {
		if(node instanceof RuleNode) {
			RuleNode ruleNode = (RuleNode)node;
			
			if(addIndent(ruleNode.getValue()))
				level += 1;
			
			if(printIndent(ruleNode.getValue())) {
				for(int i = 0; i < level; i++) {
					System.out.print("   ");
				}
			}
			
			for(Node child : ruleNode.getChildren()) {
				print(level, child);
			}
		} else if(((LeafNode)node).getToken().getTokenClass() != TigerTokenClass.EPSILON) {
			TigerToken token = ((LeafNode)node).getToken();
			
			if(printIndent(token.getTokenClass())) {
				for(int i = 0; i < level; i++) {
					System.out.print("   ");
				}
			} else if(addWhitespace(token.getTokenClass())) {
				System.out.print(" ");
			}
			
			System.out.print(token.getToken());
			System.out.print(generateNewLine(token.getTokenClass()) ? "\n" : "");
		}
	}
	
	private static Node generate(int limit, Deque<TigerSymbol> symbolStack) {
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
							int idx = (int)(rng.nextDouble() * rule.productions.size());
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
						node.getChildren().add(generate(limit - 1, newSymbolStack));
					}
					
					return node;
				}
				catch(Throwable t) {
				}
			}
		} else {
			return new LeafNode(new TigerToken((TigerTokenClass)symbol, generateTokenString((TigerTokenClass)symbol), "", 0, 0));
		}
	}
	
	private static boolean printIndent(TigerProductionRule rule) {
		switch(rule) {
			case TYPEDECL:
			case VARDECL:
			case FUNCDECL:
			case STMT:
				return true;
			default:
				return false;
		}
	}
	
	private static boolean printIndent(TigerTokenClass tokenClass) {
		switch(tokenClass) {
			case ELSE:
			case END:
			case ENDIF:
			case ENDDO:
				return true;
			default:
				return false;
		}
	}
	
	private static boolean addIndent(TigerProductionRule token) {
		switch(token) {
			case DECLSEG:
			case STMT:
				return true;
			default:
				return false;
		}
	}
	
	private static boolean addWhitespace(TigerTokenClass tokenClass) {
		switch(tokenClass) {
			case COMMA:
			case SEMICOLON:
				return false;
		}
		
		return true;
	}
	
	private static boolean generateNewLine(TigerTokenClass token) {
		switch(token) {
			case LET:
			case BEGIN:
			case THEN:
			case ELSE:
			case DO:
			case SEMICOLON:
			case IN:
				return true;
		}
		
		return false;
	}
	
	private static String generateTokenString(TigerTokenClass symbol) {
		if(symbol == TigerTokenClass.ID) {
			return generateID();
		}
		
		if(symbol == TigerTokenClass.INTLIT) {
			return String.valueOf((int)(rng.nextDouble() * 1000));
		}
		
		if(symbol == TigerTokenClass.FLOATLIT) {
			return String.format("%.3f", rng.nextDouble() * 1000);
		}
		
		String token;
		
		token = specialTokenClasses.get(symbol);
		if(token != null) {
			return token;
		}
		
		return symbol.toString().replace("_", "").toLowerCase();
	}
	
	private static char generateIDChar() {
		switch((int)(rng.nextDouble() * 4)) {
			case 0:
				return (char)((rng.nextDouble() * ('z' - 'a')) + 'a');
			case 1:
				return Character.toUpperCase((char)((rng.nextDouble() * ('z' - 'a')) + 'a'));
			case 2:
				return (char)((rng.nextDouble() * ('9' - '0')) + '0');
			default:
				return '_';
		}
	}
	
	private static String generateID() {
		String id = "";
		
		if(rng.nextBoolean()) {
			id += "_";
			
			while(rng.nextBoolean())
				id += "_";
			
			char c;
			while((c = generateIDChar()) == '_');
			
			id += c;
		} else {
			char c;
			while(Character.isDigit(c = generateIDChar()) || c == '_');
			
			id += c;
		}
		
		do {
			int len = (int)(rng.nextDouble() * 5) + 3;
			for(int i = 0; i < len; i++) {
				id += generateIDChar();
			}
			
			try {
				if(id.equals("type"))
					continue;
				
				TigerTokenClass.valueOf(id.toUpperCase());
			} catch(Exception exc) {
				break;
			}
		} while(true);
		
		return id;
	}
}
