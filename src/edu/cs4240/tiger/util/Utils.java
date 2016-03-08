package edu.cs4240.tiger.util;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class Utils {
	public static <T> void ensureValue(T value, T expected) {
		if(value != expected) {
			throw new IllegalArgumentException("Expected " + expected + ", received " + value);
		}
	}
	
	public static TigerToken getLeftmostLeaf(RuleNode node) {
		if(node.getChildren().size() == 0) {
			return null;
		}
		
		Node first = node.getChildren().get(0);
		if(first instanceof LeafNode) {
			return ((LeafNode)first).getToken();
		}
		
		return getLeftmostLeaf((RuleNode)first);
	}
	
	public static String stringify(int level, Node node) {
		String s = "";
		
		if(node instanceof RuleNode) {
			RuleNode ruleNode = (RuleNode)node;
			
			if(addIndent(ruleNode.getValue())) {
				level += 1;
			}
			
			if(printIndent(ruleNode.getValue())) {
				for(int i = 0; i < level; i++) {
					s += "   ";
				}
			}
			
			for(Node child : ruleNode.getChildren()) {
				s += stringify(level, child);
			}
		} else if(((LeafNode)node).getToken().getTokenClass() != TigerTokenClass.EPSILON) {
			TigerToken token = ((LeafNode)node).getToken();
			
			if(printIndent(token.getTokenClass())) {
				for(int i = 0; i < level; i++) {
					s += "   ";
				}
			} else if(addWhitespace(token.getTokenClass())) {
				s += " ";
			}
			
			s += token.getToken();
			s += generateNewLine(token.getTokenClass()) ? "\n" : "";
		}
		
		return s;
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
}
