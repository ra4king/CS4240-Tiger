package edu.cs4240.tiger.util;

import java.util.HashMap;

import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.parser.node.LeafNode;
import edu.cs4240.tiger.parser.node.Node;
import edu.cs4240.tiger.parser.node.RuleNode;

/**
 * @author Roi Atalla
 */
public class Utils {
	public static final HashMap<TigerTokenClass, String> specialTokenClassesToString;
	public static final HashMap<String, TigerTokenClass> specialTokenStringToClasses;
	
	static {
		specialTokenClassesToString = new HashMap<>();
		specialTokenClassesToString.put(TigerTokenClass.EPSILON, "ϵ");
		specialTokenClassesToString.put(TigerTokenClass.COMMA, ",");
		specialTokenClassesToString.put(TigerTokenClass.COLON, ":");
		specialTokenClassesToString.put(TigerTokenClass.SEMICOLON, ";");
		specialTokenClassesToString.put(TigerTokenClass.LPAREN, "(");
		specialTokenClassesToString.put(TigerTokenClass.RPAREN, ")");
		specialTokenClassesToString.put(TigerTokenClass.LBRACKET, "[");
		specialTokenClassesToString.put(TigerTokenClass.RBRACKET, "]");
		specialTokenClassesToString.put(TigerTokenClass.DOT, ".");
		specialTokenClassesToString.put(TigerTokenClass.PLUS, "+");
		specialTokenClassesToString.put(TigerTokenClass.MINUS, "-");
		specialTokenClassesToString.put(TigerTokenClass.STAR, "*");
		specialTokenClassesToString.put(TigerTokenClass.FWSLASH, "/");
		specialTokenClassesToString.put(TigerTokenClass.PERCENT, "%");
		specialTokenClassesToString.put(TigerTokenClass.EQUAL, "=");
		specialTokenClassesToString.put(TigerTokenClass.NOTEQUAL, "<>");
		specialTokenClassesToString.put(TigerTokenClass.LT, "<");
		specialTokenClassesToString.put(TigerTokenClass.GT, ">");
		specialTokenClassesToString.put(TigerTokenClass.LEQUAL, "<=");
		specialTokenClassesToString.put(TigerTokenClass.GEQUAL, ">=");
		specialTokenClassesToString.put(TigerTokenClass.AMP, "&");
		specialTokenClassesToString.put(TigerTokenClass.PIPE, "|");
		specialTokenClassesToString.put(TigerTokenClass.ASSIGN, ":=");
		
		specialTokenStringToClasses = new HashMap<>();
		specialTokenStringToClasses.put("ϵ", TigerTokenClass.EPSILON);
		specialTokenStringToClasses.put(",", TigerTokenClass.COMMA);
		specialTokenStringToClasses.put(":", TigerTokenClass.COLON);
		specialTokenStringToClasses.put(";", TigerTokenClass.SEMICOLON);
		specialTokenStringToClasses.put("(", TigerTokenClass.LPAREN);
		specialTokenStringToClasses.put(")", TigerTokenClass.RPAREN);
		specialTokenStringToClasses.put("[", TigerTokenClass.LBRACKET);
		specialTokenStringToClasses.put("]", TigerTokenClass.RBRACKET);
		specialTokenStringToClasses.put(".", TigerTokenClass.DOT);
		specialTokenStringToClasses.put("+", TigerTokenClass.PLUS);
		specialTokenStringToClasses.put("-", TigerTokenClass.MINUS);
		specialTokenStringToClasses.put("*", TigerTokenClass.STAR);
		specialTokenStringToClasses.put("/", TigerTokenClass.FWSLASH);
		specialTokenStringToClasses.put("%", TigerTokenClass.PERCENT);
		specialTokenStringToClasses.put("=", TigerTokenClass.EQUAL);
		specialTokenStringToClasses.put("<>", TigerTokenClass.NOTEQUAL);
		specialTokenStringToClasses.put("<", TigerTokenClass.LT);
		specialTokenStringToClasses.put(">", TigerTokenClass.GT);
		specialTokenStringToClasses.put("<=", TigerTokenClass.LEQUAL);
		specialTokenStringToClasses.put(">=", TigerTokenClass.GEQUAL);
		specialTokenStringToClasses.put("&", TigerTokenClass.AMP);
		specialTokenStringToClasses.put("|", TigerTokenClass.PIPE);
		specialTokenStringToClasses.put(":=", TigerTokenClass.ASSIGN);
	}
	
	public static <T> void ensureValue(T value, T expected) {
		if(value != expected) {
			throw new IllegalArgumentException("Expected " + expected + ", received " + value);
		}
	}
	
	public static TigerToken getLeftmostLeaf(RuleNode node) {
		if(node.getChildren().size() == 0) {
			return null;
		}
		
		for(Node child : node.getChildren()) {
			if(child instanceof LeafNode) {
				return ((LeafNode)child).getToken();
			}
			
			TigerToken childToken = getLeftmostLeaf((RuleNode)child);
			if(childToken != null) {
				return childToken;
			}
		}
		
		return null;
	}
}
