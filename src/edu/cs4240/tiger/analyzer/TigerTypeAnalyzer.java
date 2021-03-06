package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.HashMap;

import edu.cs4240.tiger.analyzer.TigerType.BaseType;
import edu.cs4240.tiger.analyzer.TigerType.TigerArrayType;
import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.parser.node.LeafNode;
import edu.cs4240.tiger.parser.node.Node;
import edu.cs4240.tiger.parser.node.RuleNode;

/**
 * @author Roi Atalla
 */
public class TigerTypeAnalyzer {
	public static boolean isTypeCompatibleAssign(TigerType dest, TigerType src) {
		return dest.equals(src) || (dest.equals(TigerType.FLOAT_TYPE) && src.equals(TigerType.INT_TYPE));
	}
	
	public static TigerType getNumexprType(RuleNode numexpr, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(numexpr.getRule(), TigerProductionRule.NUMEXPR);
		
		RuleNode leftChild = (RuleNode)numexpr.getChildren().get(0);
		if(leftChild.getRule() == TigerProductionRule.NUMEXPR) {
			RuleNode rightChild = (RuleNode)numexpr.getChildren().get(2);
			
			TigerType leftType = getNumexprType(leftChild, varTypes);
			TigerType rightType = getTermType(rightChild, varTypes);
			
			if(!TigerType.isNumericType(leftType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(leftChild));
			}
			
			if(!TigerType.isNumericType(rightType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(rightChild));
			}
			
			if(leftType.equals(TigerType.FLOAT_TYPE)) {
				return leftType;
			}
			
			if(rightType.equals(TigerType.FLOAT_TYPE)) {
				return rightType;
			}
			
			return leftType;
		} else {
			return getTermType(leftChild, varTypes);
		}
	}
	
	private static TigerType getTermType(RuleNode term, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(term.getRule(), TigerProductionRule.TERM);
		
		RuleNode leftChild = (RuleNode)term.getChildren().get(0);
		if(leftChild.getRule() == TigerProductionRule.TERM) {
			RuleNode rightChild = (RuleNode)term.getChildren().get(2);
			
			TigerType leftType = getTermType(leftChild, varTypes);
			TigerType rightType = getFactorType(rightChild, varTypes);
			
			if(!TigerType.isNumericType(leftType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(leftChild));
			}
			
			if(!TigerType.isNumericType(rightType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(rightChild));
			}
			
			if(leftType.equals(TigerType.FLOAT_TYPE)) {
				return leftType;
			}
			
			if(rightType.equals(TigerType.FLOAT_TYPE)) {
				return rightType;
			}
			
			return leftType;
		} else {
			return getFactorType(leftChild, varTypes);
		}
	}
	
	private static TigerType getFactorType(RuleNode factor, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(factor.getRule(), TigerProductionRule.FACTOR);
		
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
			
			switch(firstRule.getRule()) {
				case CONST:
					return TigerType.getLiteralType(((LeafNode)firstRule.getChildren().get(0)).getToken());
				default:
					throw new TigerParseException("Something went very wrong", getLeftmostLeaf(firstRule));
			}
		}
	}
	
	public static TigerType getIdOptOffsetType(RuleNode rule, HashMap<String, TigerType> varTypes) throws TigerParseException {
		LeafNode id = (LeafNode)rule.getChildren().get(0);
		
		TigerType idType = varTypes.get(id.getToken().getTokenString());
		
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
			
			TigerType indexType = getNumexprType(numexpr, varTypes);
			if(!indexType.equals(TigerType.INT_TYPE)) {
				throw new TigerParseException("Array index must be an integer type", getLeftmostLeaf(numexpr));
			}
			
			if(idType.baseType != BaseType.ARRAY) {
				throw new TigerParseException("Cannot index into non-array type", lbracket.getToken());
			}
			
			return ((TigerArrayType)idType).subType;
		}
	}
	
	public static void analyzeBoolexpr(RuleNode boolexpr, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(boolexpr.getRule(), TigerProductionRule.BOOLEXPR);
		
		RuleNode child = (RuleNode)boolexpr.getChildren().get(0);
		if(child.getRule() == TigerProductionRule.BOOLEXPR) {
			analyzeBoolexpr(child, varTypes);
			analyzeClause((RuleNode)boolexpr.getChildren().get(2), varTypes);
		} else {
			analyzeClause(child, varTypes);
		}
	}
	
	private static void analyzeClause(RuleNode clause, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(clause.getRule(), TigerProductionRule.CLAUSE);
		
		RuleNode child = (RuleNode)clause.getChildren().get(0);
		if(child.getRule() == TigerProductionRule.CLAUSE) {
			analyzeClause(child, varTypes);
			analyzePred((RuleNode)clause.getChildren().get(2), varTypes);
		} else {
			analyzePred(child, varTypes);
		}
	}
	
	private static void analyzePred(RuleNode pred, HashMap<String, TigerType> varTypes) throws TigerParseException {
		ensureValue(pred.getRule(), TigerProductionRule.PRED);
		
		Node first = pred.getChildren().get(0);
		if(first instanceof LeafNode) {
			LeafNode firstLeaf = (LeafNode)first;
			
			switch(firstLeaf.getToken().getTokenClass()) {
//				case ID:
//					if(!TigerType.BOOL_TYPE.equals(varTypes.get(firstLeaf.getTokenString().getTokenString()))) {
//						throw new TigerParseException("Not a boolean type", firstLeaf.getTokenString());
//					}
//					break;
				case LPAREN:
					analyzeBoolexpr((RuleNode)pred.getChildren().get(1), varTypes);
					break;
				default:
					throw new TigerParseException("Something went very wrong", firstLeaf.getToken());
			}
		} else {
			RuleNode leftRule = (RuleNode)first;
			RuleNode rightRule = (RuleNode)pred.getChildren().get(2);
			
			if(leftRule.getRule() != TigerProductionRule.NUMEXPR) {
				throw new TigerParseException("Something went very wrong", getLeftmostLeaf(leftRule));
			}
			
			TigerType leftType = getNumexprType(leftRule, varTypes);
			TigerType rightType = getNumexprType(rightRule, varTypes);
			
			if(!TigerType.isNumericType(leftType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(leftRule));
			}
			
			if(!TigerType.isNumericType(rightType)) {
				throw new TigerParseException("Operator can only be applied on numeric types", getLeftmostLeaf(rightRule));
			}
		}
	}
}
