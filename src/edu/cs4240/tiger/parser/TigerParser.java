package edu.cs4240.tiger.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Roi Atalla
 */
public class TigerParser {
	public TigerParser(BufferedReader source) throws IOException, TigerParseException {
//		TigerScanner scanner = new TigerScanner(source);
//		
//		Deque<TigerClasses> symbolStack = new ArrayDeque<>();
//		symbolStack.push(TigerProductionRule.PROGRAM);
//		
//		TigerToken token = scanner.nextToken();
//		if(token == null) {
//			throw new TigerParseException("Unexpected EOF");
//		}
//		
//		TigerClasses currClass = symbolStack.peek();
//		if(currClass instanceof TigerProductionRule) {
//			if(firsts.get(currClass).contains(token.getTokenClass())) {
//				System.out.println(currClass);
//				
//				TigerProductionRule rule = (TigerProductionRule)currClass;
//				for(List<TigerClasses> production : rule.productions) {
//					if(production.size() > 0 && production.get(0) == token.getTokenClass()) {
//						
//					}
//				}
//			} else if(firsts.get(currClass).contains(TigerTokenClass.EPSILON)) {
//				
//			} else {
//				throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
//			}
//		} else if(currClass == token.getTokenClass()) {
//			
//		} else {
//			throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
//		}
	}
	
	private static final HashMap<TigerProductionRule, List<TigerTokenClass>> firsts;
	
	static {
		firsts = new HashMap<>();
		
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			if(firsts.get(rule) == null) {
				firsts.put(rule, getFirsts(rule));
			}
		}
		
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			System.out.print(rule.toString().toLowerCase() + " - ");
			List<TigerTokenClass> f = firsts.get(rule);
			for(int i = 0; i < f.size(); i++) {
				for(int j = 0; j < i; j++) {
					if(f.get(j) == f.get(i)) {
						System.out.print("**");
						break;
					}
				}
				
				System.out.print(f.get(i) + " ");
			}
			System.out.println();
		}
	}
	
	private static List<TigerTokenClass> getFirsts(TigerProductionRule currentRule) {
		ArrayList<TigerTokenClass> currentFirsts = new ArrayList<>();
		
		for(List<TigerClasses> classes : currentRule.productions) {
			for(TigerClasses tigerClass : classes) {
				if(tigerClass instanceof TigerTokenClass) {
					currentFirsts.add((TigerTokenClass)tigerClass);
					break;
				} else {
					TigerProductionRule subRule = (TigerProductionRule)tigerClass;
					List<TigerTokenClass> subFirsts = getFirsts(subRule);
					
					currentFirsts.addAll(subFirsts);
					firsts.put(subRule, subFirsts);
					
					if(!subFirsts.contains(TigerTokenClass.EPSILON))
						break;
				}
			}
		}
		
		return currentFirsts;
	}
}
