package edu.cs4240.tiger.tests;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cs4240.tiger.parser.TigerClasses;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class ProductionRulesTester {
	public static void main(String[] args) {
		char arrow = '→';
		String eps = "ϵ";
		
		HashMap<String, TigerTokenClass> specialTokenClasses = new HashMap<>();
		specialTokenClasses.put(",", TigerTokenClass.COMMA);
		specialTokenClasses.put(":", TigerTokenClass.COLON);
		specialTokenClasses.put(";", TigerTokenClass.SEMICOLON);
		specialTokenClasses.put("(", TigerTokenClass.LPAREN);
		specialTokenClasses.put(")", TigerTokenClass.RPAREN);
		specialTokenClasses.put("[", TigerTokenClass.LBRACKET);
		specialTokenClasses.put("]", TigerTokenClass.RBRACKET);
		specialTokenClasses.put(".", TigerTokenClass.DOT);
		specialTokenClasses.put("+", TigerTokenClass.PLUS);
		specialTokenClasses.put("-", TigerTokenClass.MINUS);
		specialTokenClasses.put("*", TigerTokenClass.MULT);
		specialTokenClasses.put("/", TigerTokenClass.DIV);
		specialTokenClasses.put("=", TigerTokenClass.EQUAL);
		specialTokenClasses.put("<>", TigerTokenClass.NOTEQUAL);
		specialTokenClasses.put("<", TigerTokenClass.LT);
		specialTokenClasses.put(">", TigerTokenClass.GT);
		specialTokenClasses.put("<=", TigerTokenClass.LEQUAL);
		specialTokenClasses.put(">=", TigerTokenClass.GEQUAL);
		specialTokenClasses.put("&", TigerTokenClass.AMP);
		specialTokenClasses.put("|", TigerTokenClass.PIPE);
		specialTokenClasses.put(":=", TigerTokenClass.ASSIGN);
		
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			for(List<TigerClasses> classes : rule.productions) {
				System.out.print(rule.toString().toLowerCase() + " " + arrow + " ");
				
				if(classes.isEmpty()) {
					System.out.println(eps);
				} else {
					for(TigerClasses c : classes) {
						if(c instanceof TigerTokenClass && specialTokenClasses.containsValue(c)) {
							for(String token : specialTokenClasses.keySet()) {
								if(specialTokenClasses.get(token) == c) {
									System.out.print(token + " ");
									break;
								}
							}
						} else
							System.out.print(c.toString().toLowerCase() + " ");
					}
					System.out.println();
				}
			}
		}
		
		System.out.println("\n");
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			firsts(0, new ArrayList<>(), rule);
		}
	}
	
	private static void firsts(int indent, List<TigerProductionRule> rulesVisited, TigerProductionRule currentRule) {
		for(int i = 0; i < indent; i++) {
			System.out.print("   ");
		}
		
		System.out.print(currentRule);
		
		if(rulesVisited.contains(currentRule)) {
			System.out.println(" - already visited!");
		} else {
			System.out.println();
			
			rulesVisited.add(currentRule);
			
			currentRule.productions.forEach((classes) -> {
				if(classes.size() > 0 && classes.get(0) instanceof TigerProductionRule) {
					firsts(indent + 1, rulesVisited, (TigerProductionRule)classes.get(0));
				}
			});
		}
	}
}
