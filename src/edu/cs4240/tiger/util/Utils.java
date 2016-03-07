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
	
	public static boolean isTypeCompatibleAssign(RuleNode dest, RuleNode src) {
		if(dest.equals(src)) {
			return true;
		}
		
		if(dest.getChildren().size() == 1 && src.getChildren().size() == 1) {
			LeafNode destLeaf = (LeafNode)dest.getChildren().get(0);
			LeafNode srcLeaf = (LeafNode)src.getChildren().get(0);
			
			if(destLeaf.getToken().getTokenClass() == TigerTokenClass.FLOAT &&
			     srcLeaf.getToken().getTokenClass() == TigerTokenClass.INT) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isTypeCompatibleCompare(RuleNode src1, RuleNode src2) {
		return src1.equals(src2) || (isNumericType(src1) && isNumericType(src2));
	}
	
	public static boolean isNumericType(RuleNode type) {
		if(type.getChildren().size() == 1) {
			LeafNode leaf = (LeafNode)type.getChildren().get(0);
			return leaf.getToken().getTokenClass() == TigerTokenClass.INT || leaf.getToken().getTokenClass() == TigerTokenClass.FLOAT;
		}
		
		return false;
	}
	
	public static TigerToken getLiteralType(TigerToken token) {
		if(token.getTokenClass() == TigerTokenClass.INTLIT) {
			return new TigerToken(TigerTokenClass.INT, "int", "", 0, 0);
		}
		if(token.getTokenClass() == TigerTokenClass.FLOATLIT) {
			return new TigerToken(TigerTokenClass.FLOAT, "float", "", 0, 0);
		}
		if(token.getTokenClass() == TigerTokenClass.TRUE || token.getTokenClass() == TigerTokenClass.FALSE) {
			return new TigerToken(TigerTokenClass.BOOL, "bool", "", 0, 0);
		}
		
		throw new IllegalArgumentException("Argument is not a literal.");
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
