package edu.cs4240.tiger.util;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
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
	
	public static LeafNode getLeftmostLeaf(RuleNode node) {
		if(node.getChildren().size() == 0) {
			return null;
		}
		
		Node first = node.getChildren().get(0);
		if(first instanceof LeafNode) {
			return (LeafNode)first;
		}
		
		return getLeftmostLeaf((RuleNode)first);
	}
	
	public static boolean isTypeCompatible(RuleNode dest, RuleNode src) {
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
}
