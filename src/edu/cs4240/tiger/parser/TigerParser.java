package edu.cs4240.tiger.parser;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
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
	public interface Node {
	}
	
	public class RuleNode implements Node {
		private TigerProductionRule value;
		private List<Node> children;
		
		public RuleNode() {
			this((TigerProductionRule)null);
		}
		
		public RuleNode(RuleNode node) {
			this.value = node.value;
			this.children = new ArrayList<>(node.children);
		}
		
		public RuleNode(TigerProductionRule value, Node... children) {
			this.value = value;
			this.children = new ArrayList<>(Arrays.asList(children));
		}
		
		public TigerProductionRule getValue() {
			return value;
		}
		
		public void setValue(TigerProductionRule value) {
			this.value = value;
		}
		
		public List<Node> getChildren() {
			return children;
		}
		
		@Override
		public String toString() {
			if(children.size() == 0) {
				return value.toString().toLowerCase();
			}
			
			String s = "(" + value.toString().toLowerCase();
			for(Node child : children) {
				s += " " + child.toString();
			}
			s += ")";
			return s;
		}
	}
	
	public class LeafNode implements Node {
		private TigerToken token;
		
		public LeafNode() {
		}
		
		public LeafNode(TigerToken token) {
			this.token = token;
		}
		
		public TigerToken getToken() {
			return token;
		}
		
		@Override
		public String toString() {
			return token.getToken();
		}
	}
	
	private Node ast;
	private Queue<TigerToken> tokenQueue;
	private Deque<TigerSymbol> symbolStack;
	
	public TigerParser(TigerScanner scanner) throws IOException, TigerParseException {
		tokenQueue = new LinkedList<>();
		
		while(true) {
			TigerToken token = scanner.nextToken();
			if(token == null) {
				break;
			}
			tokenQueue.offer(token);
		}
		
		symbolStack = new ArrayDeque<>();
		symbolStack.push(TigerProductionRule.PROGRAM);
	}
	
	public Queue<TigerToken> getTokenQueue() {
		return new LinkedList<>(tokenQueue);
	}
	
	public Node parse() throws TigerParseException {
		if(ast == null) {
			ast = parse(tokenQueue, symbolStack);
			cleanupTails(ast);
			cleanupRecursiveRules(ast);
		}
		
		return ast;
	}
	
	private void cleanupTails(Node node) throws TigerParseException {
		if(node instanceof RuleNode) {
			RuleNode ruleNode = (RuleNode)node;
			
			for(int i = 0; i < ruleNode.getChildren().size(); i++) {
				Node child = ruleNode.getChildren().get(i);
				
				if(child instanceof RuleNode) {
					RuleNode childRule = (RuleNode)child;
					cleanupTails(childRule);
					
					if(childRule.getValue().toString().endsWith("_TAIL")) {
						ruleNode.getChildren().remove(i);
						
						for(int j = 0; j < childRule.getChildren().size(); j++) {
							ruleNode.getChildren().add(i + j, childRule.getChildren().get(j));
						}
						
						i += childRule.getChildren().size() - 1;
					}
				}
			}
		}
	}
	
	private void cleanupRecursiveRules(Node node) throws TigerParseException {
		RuleNode ruleNode = (RuleNode)node;
		for(Node child : ruleNode.getChildren()) {
			if(child instanceof RuleNode) {
				RuleNode childRule = (RuleNode)child;
				
				cleanupRecursiveRule(ruleNode, childRule, TigerProductionRule.NUMEXPR, TigerProductionRule.TERM);
				cleanupRecursiveRule(ruleNode, childRule, TigerProductionRule.TERM, TigerProductionRule.FACTOR);
				
				cleanupRecursiveRules(child);
			}
		}
	}
	
	private void cleanupRecursiveRule(RuleNode ruleNode, RuleNode childRule, TigerProductionRule parent, TigerProductionRule child) throws TigerParseException {
		if(ruleNode.getValue() != parent && childRule.getValue() == parent) {
			while(true) {
				if(childRule.getChildren().size() == 3) {
					RuleNode rightExpr = (RuleNode)childRule.getChildren().remove(2);
					RuleNode op = (RuleNode)childRule.getChildren().remove(1);
					RuleNode leftExpr = (RuleNode)childRule.getChildren().remove(0);
					
					if(leftExpr.getValue() == child) {
						leftExpr = new RuleNode(parent, leftExpr);
					}
					
					if(rightExpr.getValue() == child) {
						childRule.getChildren().add(0, leftExpr);
						childRule.getChildren().add(1, op);
						childRule.getChildren().add(2, rightExpr);
						break;
					}
					
					if(rightExpr.getChildren().size() == 1) {
						childRule.getChildren().add(0, leftExpr);
						childRule.getChildren().add(1, op);
						childRule.getChildren().add(2, rightExpr.getChildren().get(0));
						break;
					} else {
						if(rightExpr.getChildren().size() != 3) {
							throw new TigerParseException("Something bad happened!");
						}
						
						childRule.getChildren().add(0, new RuleNode(parent, leftExpr, op, rightExpr.getChildren().get(0)));
						childRule.getChildren().add(1, rightExpr.getChildren().get(1));
						childRule.getChildren().add(2, rightExpr.getChildren().get(2));
					}
				} else {
					break;
				}
			}
		}
	}
	
	private Node parse(Queue<TigerToken> tokenQueue, Deque<TigerSymbol> symbolStack) throws TigerParseException {
		TigerToken token = tokenQueue.peek();
		TigerSymbol symbol = symbolStack.pop();
		
		if(symbol instanceof TigerProductionRule) {
			TigerProductionRule rule = (TigerProductionRule)symbol;
			
			Pair<TigerTokenClass, List<TigerSymbol>> epsilon = null;
			
			TigerParseException innerParseException = null;
			
			for(Pair<TigerTokenClass, List<TigerSymbol>> pair : firsts.get(rule)) {
				if(epsilon == null && pair.getKey() == TigerTokenClass.EPSILON) {
					epsilon = pair;
				} else if(token != null && pair.getKey() == token.getTokenClass()) {
					Queue<TigerToken> newTokenQueue = new LinkedList<>(tokenQueue);
					
					Deque<TigerSymbol> newSymbolStack = new ArrayDeque<>();
					for(int i = pair.getValue().size() - 1; i >= 0; i--) {
						newSymbolStack.push(pair.getValue().get(i));
					}
					
					RuleNode node = new RuleNode(rule);
					
					try {
						while(!newSymbolStack.isEmpty()) {
							node.getChildren().add(parse(newTokenQueue, newSymbolStack));
						}
					}
					catch(TigerParseException exc) {
						innerParseException = exc;
						continue;
					}
					
					tokenQueue.clear();
					tokenQueue.addAll(newTokenQueue);
					
					return node;
				}
			}
			
			if(epsilon != null) {
				return new RuleNode(rule);
			} else if(innerParseException != null) {
				throw innerParseException;
			} else if(token == null) {
				throw new TigerParseException("Unexpected end-of-file.");
			} else {
				throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
			}
		} else if(token == null) {
			throw new TigerParseException("Unexpected end-of-file. Expected token " + symbol);
		} else if(symbol == token.getTokenClass()) {
			return new LeafNode(tokenQueue.remove());
		} else {
			throw new TigerParseException("Unexpected token '" + token.getToken() + "'", token);
		}
	}
	
	private static final HashMap<TigerProductionRule, List<Pair<TigerTokenClass, List<TigerSymbol>>>> firsts;
	
	static {
		firsts = new HashMap<>();
		
		// Generated the firsts table for each rule
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			firsts.put(rule, getFirsts(rule));
		}
		
		// Remove all duplicate firsts for each symbol for each rule
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			List<Pair<TigerTokenClass, List<TigerSymbol>>> f = firsts.get(rule);
			for(Iterator<Pair<TigerTokenClass, List<TigerSymbol>>> it = f.iterator(); it.hasNext(); ) {
				Pair<TigerTokenClass, List<TigerSymbol>> pair = it.next();
				
				for(Pair<TigerTokenClass, List<TigerSymbol>> innerPair : f) {
					if(pair == innerPair) {
						break;
					}
					
					if(pair.getKey() == innerPair.getKey() && pair.getValue().equals(innerPair.getValue())) {
						it.remove();
						break;
					}
				}
			}
		}
	}
	
	private static List<Pair<TigerTokenClass, List<TigerSymbol>>> getFirsts(TigerProductionRule currentRule) {
		List<Pair<TigerTokenClass, List<TigerSymbol>>> currentFirsts = new ArrayList<>();
		
		boolean foundEps = false;
		
		for(List<TigerSymbol> production : currentRule.productions) {
			for(TigerSymbol symbol : production) {
				if(symbol instanceof TigerTokenClass) {
					if(symbol != TigerTokenClass.EPSILON) {
						foundEps = false;
					}
					
					currentFirsts.add(new Pair<>((TigerTokenClass)symbol, production));
					break;
				} else {
					TigerProductionRule subRule = (TigerProductionRule)symbol;
					
					List<Pair<TigerTokenClass, List<TigerSymbol>>> subFirsts = getFirsts(subRule);
					
					foundEps = false;
					
					for(Iterator<Pair<TigerTokenClass, List<TigerSymbol>>> it = subFirsts.iterator(); it.hasNext(); ) {
						Pair<TigerTokenClass, List<TigerSymbol>> pair = it.next();
						
						if(pair.getKey() == TigerTokenClass.EPSILON) {
							foundEps = true;
							it.remove();
						} else {
							pair.setValue(production);
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
