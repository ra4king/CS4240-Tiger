package edu.cs4240.tiger.generator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerSymbol;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerSourceGenerator {
	private static HashMap<TigerTokenClass, String> specialTokenClasses;
	
	public static void main(String[] args) {
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
		
		int limit = (int)(Math.random() * 20 + 10);
		long before = System.nanoTime();
		Queue<TigerToken> program = generate(limit, symbolStack);
		long time = System.nanoTime() - before;
		
		System.out.printf("This program generated in %.3f ms, using limit = %d:\n\n", time / 1e6, limit);
		
		for(TigerToken token; program.size() > 0; ) {
			token = program.remove();
			System.out.print(token.getToken());
			System.out.print(generateNewLine(token) ? "\n" : " ");
		}
	}
	
	private static Queue<TigerToken> generate(int limit, Deque<TigerSymbol> symbolStack) {
		Queue<TigerToken> tokenQueue = new LinkedList<>();
		
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
							int idx = (int)(Math.random() * rule.productions.size());
							production = rule.productions.get(idx);
							
							if(production.get(0) != TigerTokenClass.EPSILON) {
								break;
							}
						}
					}
					
					Queue<TigerToken> newTokenQueue = new LinkedList<>();
					Deque<TigerSymbol> newSymbolStack = new ArrayDeque<>();
					for(int i = production.size() - 1; i >= 0; i--) {
						newSymbolStack.push(production.get(i));
					}
					
					while(!newSymbolStack.isEmpty()) {
						newTokenQueue.addAll(generate(limit - 1, newSymbolStack));
					}
					
					tokenQueue.addAll(newTokenQueue);
					
					break;
				}
				catch(Throwable t) {
				}
			}
			
			return tokenQueue;
		} else {
			if(symbol != TigerTokenClass.EPSILON) {
				tokenQueue.add(new TigerToken((TigerTokenClass)symbol, generateTokenString((TigerTokenClass)symbol), "", 0, 0));
			}
			return tokenQueue;
		}
	}
	
	private static boolean generateNewLine(TigerToken token) {
		switch(token.getTokenClass()) {
			case LET:
			case BEGIN:
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
			return String.valueOf((int)(Math.random() * 1000));
		}
		
		if(symbol == TigerTokenClass.FLOATLIT) {
			return String.format("%.3f", Math.random() * 1000);
		}
		
		String token;
		
		token = specialTokenClasses.get(symbol);
		if(token != null) {
			return token;
		}
		
		return symbol.toString().replace("_", "").toLowerCase();
	}
	
	private static String generateID() {
		String id;
		do {
			id = "";
			int len = (int)(Math.random() * 5) + 3;
			for(int i = 0; i < len; i++) {
				id += (char)((Math.random() * ('z' - 'a')) + 'a');
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
