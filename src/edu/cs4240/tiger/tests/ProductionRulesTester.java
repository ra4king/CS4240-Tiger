package edu.cs4240.tiger.tests;

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
	}
}
