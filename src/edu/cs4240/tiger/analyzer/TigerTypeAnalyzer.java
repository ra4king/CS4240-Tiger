package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.HashMap;

import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerTypeAnalyzer {
	public static RuleNode getNumexprType(RuleNode numexpr, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(numexpr.getValue(), TigerProductionRule.NUMEXPR);
		
		RuleNode child = (RuleNode)numexpr.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.NUMEXPR) {
			RuleNode leftTypeRule = getNumexprType(child, varTypes);
			RuleNode rightTypeRule = getTermType((RuleNode)numexpr.getChildren().get(2), varTypes);
			
			LeafNode leftType = (LeafNode)leftTypeRule.getChildren().get(0);
			LeafNode rightType = (LeafNode)rightTypeRule.getChildren().get(0);
			
			if(leftType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     leftType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", leftType.getToken());
			}
			
			if(rightType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     rightType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", rightType.getToken());
			}
			
			if(leftType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return leftTypeRule;
			}
			
			if(rightType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return rightTypeRule;
			}
			
			return leftTypeRule;
		} else {
			return getTermType(child, varTypes);
		}
	}
	
	private static RuleNode getTermType(RuleNode term, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(term.getValue(), TigerProductionRule.TERM);
		
		RuleNode child = (RuleNode)term.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.TERM) {
			RuleNode leftTypeRule = getTermType(child, varTypes);
			RuleNode rightTypeRule = getFactorType((RuleNode)term.getChildren().get(2), varTypes);
			
			LeafNode leftType = (LeafNode)leftTypeRule.getChildren().get(0);
			LeafNode rightType = (LeafNode)rightTypeRule.getChildren().get(0);
			
			if(leftType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     leftType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", leftType.getToken());
			}
			
			if(rightType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     rightType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", rightType.getToken());
			}
			
			if(leftType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return leftTypeRule;
			}
			
			if(rightType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return rightTypeRule;
			}
			
			return leftTypeRule;
		} else {
			return getFactorType(child, varTypes);
		}
	}
	
	private static RuleNode getFactorType(RuleNode factor, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(factor.getValue(), TigerProductionRule.FACTOR);
		
		Node first = factor.getChildren().get(0);
		if(first instanceof LeafNode) {
			LeafNode firstLeaf = (LeafNode)first;
			
			switch(firstLeaf.getToken().getTokenClass()) {
				case ID:
					return getIdOptOffsetType(factor, varTypes);
				case LPAREN:
					return getNumexprType((RuleNode)factor.getChildren().get(1), varTypes);
				default:
					throw new TigerParseException("Something went very wrong", firstLeaf.getToken());
			}
		} else {
			RuleNode firstRule = (RuleNode)first;
			
			switch(firstRule.getValue()) {
				case CONST:
					return new RuleNode(TigerProductionRule.TYPE, new LeafNode(getLiteralType(((LeafNode)firstRule.getChildren().get(0)).getToken())));
				default:
					throw new TigerParseException("Something went very wrong", getLeftmostLeaf(firstRule));
			}
		}
	}
	
	public static RuleNode getIdOptOffsetType(RuleNode rule, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		LeafNode id = (LeafNode)rule.getChildren().get(0);
		
		RuleNode idType = varTypes.get(id.getToken().getToken());
		
		if(idType == null) {
			throw new TigerParseException("Undeclared variable", id.getToken());
		}
		
		if(rule.getChildren().size() == 1) {
			return idType;
		} else {
			LeafNode lbracket, rbracket;
			RuleNode numexpr;
			
			if(rule.getChildren().get(1) instanceof RuleNode) { // optoffset rule
				if(((RuleNode)rule.getChildren().get(1)).getChildren().size() == 0) {
					return idType;
				}
				
				lbracket = (LeafNode)((RuleNode)rule.getChildren().get(1)).getChildren().get(0);
				rbracket = (LeafNode)((RuleNode)rule.getChildren().get(1)).getChildren().get(2);
				numexpr = (RuleNode)((RuleNode)rule.getChildren().get(1)).getChildren().get(1);
			} else {
				lbracket = (LeafNode)rule.getChildren().get(1);
				rbracket = (LeafNode)rule.getChildren().get(3);
				numexpr = (RuleNode)rule.getChildren().get(2);
			}
			
			ensureValue(lbracket.getToken().getTokenClass(), TigerTokenClass.LBRACKET);
			ensureValue(rbracket.getToken().getTokenClass(), TigerTokenClass.RBRACKET);
			
			RuleNode indexType = getNumexprType(numexpr, varTypes);
			if(((LeafNode)indexType.getChildren().get(0)).getToken().getTokenClass() != TigerTokenClass.INT) {
				throw new TigerParseException("Array index must be an integer type", getLeftmostLeaf(numexpr));
			}
			
			if(((LeafNode)idType.getChildren().get(0)).getToken().getTokenClass() != TigerTokenClass.ARRAY) {
				throw new TigerParseException("Cannot index into non-array type", getLeftmostLeaf(numexpr));
			}
			
			return (RuleNode)idType.getChildren().get(5);
		}
	}
	
	public static void analyzeBoolexpr(RuleNode boolexpr, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(boolexpr.getValue(), TigerProductionRule.BOOLEXPR);
		
		RuleNode child = (RuleNode)boolexpr.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.BOOLEXPR) {
			analyzeBoolexpr(child, varTypes);
			analyzeClause((RuleNode)boolexpr.getChildren().get(2), varTypes);
		} else {
			analyzeClause(child, varTypes);
		}
	}
	
	private static void analyzeClause(RuleNode clause, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(clause.getValue(), TigerProductionRule.CLAUSE);
		
		RuleNode child = (RuleNode)clause.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.CLAUSE) {
			analyzeClause(child, varTypes);
			analyzePred((RuleNode)clause.getChildren().get(2), varTypes);
		} else {
			analyzePred(child, varTypes);
		}
	}
	
	private static void analyzePred(RuleNode pred, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(pred.getValue(), TigerProductionRule.PRED);
		
		Node first = pred.getChildren().get(0);
		if(first instanceof LeafNode) {
			LeafNode firstLeaf = (LeafNode)first;
			
			if(firstLeaf.getToken().getTokenClass() != TigerTokenClass.LPAREN) {
				throw new TigerParseException("Something went very wrong", firstLeaf.getToken());
			}
			
			analyzeBoolexpr((RuleNode)pred.getChildren().get(1), varTypes);
		} else {
			RuleNode leftRule = (RuleNode)first;
			RuleNode rightRule = (RuleNode)pred.getChildren().get(2);
			
			if(leftRule.getValue() != TigerProductionRule.NUMEXPR) {
				throw new TigerParseException("Something went very wrong", getLeftmostLeaf(leftRule));
			}
			
			RuleNode leftTypeRule = getNumexprType(leftRule, varTypes);
			RuleNode rightTypeRule = getNumexprType(rightRule, varTypes);
			LeafNode leftType = (LeafNode)leftTypeRule.getChildren().get(0);
			LeafNode rightType = (LeafNode)rightTypeRule.getChildren().get(0);
			
			if(leftType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     leftType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(leftRule));
			}
			
			if(rightType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     rightType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(rightRule));
			}
		}
	}
}
