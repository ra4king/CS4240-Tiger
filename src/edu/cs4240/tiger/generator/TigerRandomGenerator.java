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
public class TigerRandomGenerator {
	public static Node generate(Random rng, int limit) {
		Deque<TigerSymbol> symbolStack = new ArrayDeque<>();
		symbolStack.add(TigerProductionRule.PROGRAM);
		return generate(rng, limit, symbolStack);
	}
	
	private static Node generate(Random rng, int limit, Deque<TigerSymbol> symbolStack) {
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
						int trycount = rng.nextInt(3) + 1;
						while(trycount-- > 0) {
							int idx = rng.nextInt(rule.productions.size());
							production = rule.productions.get(idx);
							
							// Rule 52 exception
							if(rule == TigerProductionRule.PRED && production.get(0) == TigerTokenClass.LPAREN) {
								trycount++;
							} else if(production.get(0) != TigerTokenClass.EPSILON) {
								break;
							}
						}
					}
					
					if(production == null) {
						continue;
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
			return GeneratorUtils.generateID(rng);
		}
		
		if(symbol == TigerTokenClass.INTLIT) {
			return GeneratorUtils.generateIntlit(rng);
		}
		
		if(symbol == TigerTokenClass.FLOATLIT) {
			return GeneratorUtils.generateFloatlit(rng);
		}
		
		String token;
		
		token = Utils.specialTokenClassesToString.get(symbol);
		if(token != null) {
			return token;
		}
		
		return symbol.toString().replace("_", "").toLowerCase();
	}
}
