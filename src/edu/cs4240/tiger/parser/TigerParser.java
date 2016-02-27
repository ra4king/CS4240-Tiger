package edu.cs4240.tiger.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.cs4240.tiger.util.Pair;

/**
 * @author Roi Atalla
 */
public class TigerParser {
	public TigerParser(BufferedReader source) throws IOException, TigerParseException {
		TigerScanner scanner = new TigerScanner(source);
		
		Queue<TigerToken> tokenQueue = new LinkedList<>();
		Deque<TigerClasses> symbolStack = new ArrayDeque<>();
		symbolStack.push(TigerProductionRule.PROGRAM);
		
		while(true) {
			TigerToken token = scanner.nextToken();
			if(token == null)
				break;
			tokenQueue.offer(token);
		}
		
		while(!symbolStack.isEmpty()) {
			TigerToken token = tokenQueue.peek();
			TigerClasses currClass = symbolStack.peek();
			if(currClass instanceof TigerProductionRule) {
				TigerProductionRule rule = (TigerProductionRule)currClass;
				
				System.out.println(rule);
				
				if(rule.productions.size() == 1) {
					symbolStack.pop();
					push(symbolStack, rule.productions.get(0));
				} else {
					Pair<TigerTokenClass, List<TigerClasses>> epsilon = null;
					
					boolean foundProduction = false;
					
					for(Pair<TigerTokenClass, List<TigerClasses>> pair : firsts.get(rule)) {
						if(epsilon == null && pair.getKey() == TigerTokenClass.EPSILON) {
							epsilon = pair;
						} else if(token != null && pair.getKey() == token.getTokenClass()) {
							foundProduction = true;
							symbolStack.pop();
							push(symbolStack, pair.getValue());
							break;
						}
					}
					
					if(!foundProduction) {
						if(epsilon != null) {
							System.out.println("ϵ");
							symbolStack.pop();
							
							// if epsilon.getValues().size() == 0, record this rule as epsilon
							// else expand rule
						} else if(token == null) {
							throw new TigerParseException("Unexpected end-of-file.");
						} else {
							throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
						}
					}
				}
			} else if(token == null) {
				throw new TigerParseException("Unexpected end-of-file. Expected token " + currClass);
			} else if(currClass == token.getTokenClass()) {
				System.out.println(tokenQueue.remove());
				symbolStack.pop();
			} else {
				throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
			}
		}
		
		System.out.println();
	}
	
	private void push(Deque<TigerClasses> stack, List<TigerClasses> production) {
		for(int i = production.size() - 1; i >= 0; i--) {
			stack.push(production.get(i));
		}
	}
	
	private static final HashMap<TigerProductionRule, List<Pair<TigerTokenClass, List<TigerClasses>>>> firsts;
	
	static {
		firsts = new HashMap<>();
		
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			firsts.put(rule, getFirsts(rule));
		}
		
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			System.out.print(rule.toString().toLowerCase() + " -");
			List<Pair<TigerTokenClass, List<TigerClasses>>> f = firsts.get(rule);
			for(Iterator<Pair<TigerTokenClass, List<TigerClasses>>> it = f.iterator(); it.hasNext(); ) {
				Pair<TigerTokenClass, List<TigerClasses>> pair = it.next();
				
				boolean removed = false;
				for(Pair<TigerTokenClass, List<TigerClasses>> innerPair : f) {
					if(pair == innerPair) {
						break;
					}
					
					if(pair.getKey() == innerPair.getKey()) {
						if(pair.getValue().equals(innerPair.getValue())) {
							it.remove();
							removed = true;
						} else {
							System.out.print("**");
						}
						
						break;
					}
				}
				
				if(!removed) {
					System.out.print(" " + pair.getKey().toString().toLowerCase() + "{" + TigerProductionRule.printRule(rule, pair.getValue()) + "}");
				}
			}
			System.out.println();
		}
		
		System.out.println();
	}
	
	private static List<Pair<TigerTokenClass, List<TigerClasses>>> getFirsts(TigerProductionRule currentRule) {
		List<Pair<TigerTokenClass, List<TigerClasses>>> currentFirsts = new ArrayList<>();
		
		boolean foundEps = false;
		
		for(List<TigerClasses> classes : currentRule.productions) {
			for(TigerClasses tigerClass : classes) {
				if(tigerClass instanceof TigerTokenClass) {
					if(tigerClass != TigerTokenClass.EPSILON) {
						foundEps = false;
					}
					
					currentFirsts.add(new Pair<>((TigerTokenClass)tigerClass, classes));
					break;
				} else {
					TigerProductionRule subRule = (TigerProductionRule)tigerClass;
					
					List<Pair<TigerTokenClass, List<TigerClasses>>> subFirsts = getFirsts(subRule);
					
					foundEps = false;
					
					for(Iterator<Pair<TigerTokenClass, List<TigerClasses>>> it = subFirsts.iterator(); it.hasNext(); ) {
						Pair<TigerTokenClass, List<TigerClasses>> pair = it.next();
						
						if(pair.getKey() == TigerTokenClass.EPSILON) {
							foundEps = true;
							it.remove();
						} else {
							pair.setValue(classes);
						}
					}
					
					currentFirsts.addAll(subFirsts);
					
					if(!foundEps) {
						break;
					}
				}
			}
			
			if(foundEps) {
				currentFirsts.add(new Pair<>(TigerTokenClass.EPSILON, new ArrayList<>()));
			}
		}
		
		return currentFirsts;
	}
}
