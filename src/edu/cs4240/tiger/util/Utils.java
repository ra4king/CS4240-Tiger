package edu.cs4240.tiger.util;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
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
}
