package edu.cs4240.tiger.util;

import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.parser.node.LeafNode;
import edu.cs4240.tiger.parser.node.Node;
import edu.cs4240.tiger.parser.node.RuleNode;

/**
 * @author Roi Atalla
 */
public class StringifyTigerAST {
	public static String stringifyAST(Node node) {
		return stringifyAST(0, node);
	}
	
	private static String stringifyAST(int level, Node node) {
		String s = "";
		
		if(node instanceof RuleNode) {
			RuleNode ruleNode = (RuleNode)node;
			
			if(addIndent(ruleNode.getRule())) {
				level += 1;
			}
			
			if(printIndent(ruleNode.getRule())) {
				for(int i = 0; i < level; i++) {
					s += "   ";
				}
			}
			
			for(Node child : ruleNode.getChildren()) {
				s += stringifyAST(level, child);
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
			
			s += token.getTokenString();
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
			case LET:
			case TYPE_:
			case FUNC:
			case VAR:
			case IN:
			case END:
			case IF:
			case WHILE:
			case FOR:
			case BREAK:
			case RETURN:
			case COMMA:
			case SEMICOLON:
				return false;
			default:
				return true;
		}
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
			default:
				return false;
		}
	}
}
