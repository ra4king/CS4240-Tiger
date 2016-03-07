package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.HashMap;
import java.util.List;

import edu.cs4240.tiger.parser.TigerParseException;
import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.util.Pair;

/**
 * @author Roi Atalla
 */
public class TigerAnalyzer {
	private RuleNode ast;
	private TigerSymbolTable symbolTable;
	
	public TigerAnalyzer(RuleNode ast) {
		this.ast = ast;
	}
	
	public void run() throws TigerParseException {
		symbolTable = new TigerSymbolTable(ast);

		analyzeFunctions();
		analyzeProgramStatements();
	}
	
	private void analyzeProgramStatements() throws TigerParseException {
		analyzeStatements((RuleNode)ast.getChildren().get(3), symbolTable.getVariables(), false);
	}
	
	private void analyzeStatements(RuleNode stmts, HashMap<String, RuleNode> varTypes, boolean insideLoop) throws TigerParseException {
		ensureValue(stmts.getValue(), TigerProductionRule.STMTS);
		
		if(stmts.getChildren().size() == 0) {
			return;
		}
		
		RuleNode fullstmt = (RuleNode)stmts.getChildren().get(0);
		ensureValue(fullstmt.getValue(), TigerProductionRule.FULLSTMT);
		analyzeStatement((RuleNode)fullstmt.getChildren().get(0), varTypes, insideLoop);
		
		if(stmts.getChildren().size() > 1) {
			analyzeStatements((RuleNode)stmts.getChildren().get(1), varTypes, insideLoop);
		}
	}
	
	private void analyzeStatement(RuleNode stmt, HashMap<String, RuleNode> varTypes, boolean insideLoop) throws TigerParseException {
		ensureValue(stmt.getValue(), TigerProductionRule.STMT);
		
		Node first = stmt.getChildren().get(0);
		if(first instanceof RuleNode) {
			RuleNode firstRule = (RuleNode)first;
			
			switch(firstRule.getValue()) {
				case LVALUE:
					RuleNode lvalueType = getIdOptOffsetType((RuleNode)stmt.getChildren().get(0), varTypes);
					RuleNode numexprType = getNumexprType((RuleNode)stmt.getChildren().get(2), varTypes);
					
					if(!isTypeCompatibleAssign(lvalueType, numexprType)) {
						throw new TigerParseException("Incompatible types", getLeftmostLeaf(numexprType));
					}
					
					break;
				case OPTSTORE:
					// stmt -> optstore id ( numexprs )
					// optstore -> e | lvalue :=
					
					TigerToken functionToken = ((LeafNode)stmt.getChildren().get(1)).getToken();
					Pair<RuleNode, List<Pair<String, RuleNode>>> function = symbolTable.getFunctions().get(functionToken.getToken());
					
					RuleNode optstore = (RuleNode)stmt.getChildren().get(0);
					if(optstore.getChildren().size() > 0) {
						lvalueType = getIdOptOffsetType((RuleNode)optstore.getChildren().get(0), varTypes);
						
						if(!isTypeCompatibleAssign(lvalueType, function.getKey())) {
							throw new TigerParseException("Return type incompatible with left-hand side type", functionToken);
						}
					}
					
					RuleNode numexprs = (RuleNode)stmt.getChildren().get(3);
					if(function == null) {
						throw new TigerParseException("No such function found", ((LeafNode)stmt.getChildren().get(1)).getToken());
					}
					
					if(numexprs.getChildren().size() == 0 && function.getValue().size() != 0) {
						throw new TigerParseException("Missing arguments to function", functionToken);
					}
					
					if(numexprs.getChildren().size() == 1) {
						RuleNode neexprs = (RuleNode)numexprs.getChildren().get(0);
						
						for(Pair<String, RuleNode> arg : function.getValue()) {
							if(neexprs == null) {
								throw new TigerParseException("Argument mismatch", functionToken);
							}
							
							RuleNode numexpr = (RuleNode)neexprs.getChildren().get(0);
							RuleNode argType = getNumexprType(numexpr, varTypes);
							
							if(!isTypeCompatibleAssign(arg.getValue(), argType)) {
								throw new TigerParseException("Incompatible types", getLeftmostLeaf(numexpr));
							}
							
							if(neexprs.getChildren().size() == 3)
								neexprs = (RuleNode)neexprs.getChildren().get(2);
							else
								neexprs = null;
						}
					}
					
					break;
				default:
					throw new TigerParseException("Expected LVALUE or OPTSTORE, received " + firstRule.getValue());
			}
		} else {
			LeafNode firstLeaf = (LeafNode)first;
			
			switch(firstLeaf.getToken().getTokenClass()) {
				case IF:
					analyzeBoolexpr((RuleNode)stmt.getChildren().get(1), varTypes);
					analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, insideLoop);
					
					if(((LeafNode)stmt.getChildren().get(4)).getToken().getTokenClass() == TigerTokenClass.ELSE) {
						analyzeStatements((RuleNode)stmt.getChildren().get(5), varTypes, insideLoop);
					}
					
					break;
				case WHILE:
					analyzeBoolexpr((RuleNode)stmt.getChildren().get(1), varTypes);
					analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, true);
					
					break;
				case FOR:
					RuleNode idType = varTypes.get(((LeafNode)stmt.getChildren().get(1)).getToken().getToken());
					if(idType == null) {
						throw new TigerParseException("Undeclared variable", firstLeaf.getToken());
					}
					
					if(((LeafNode)idType.getChildren().get(0)).getToken().getTokenClass() != TigerTokenClass.INT) {
						throw new TigerParseException("Iterating variable must be of integer type", ((LeafNode)stmt.getChildren().get(1)).getToken());
					}
					
					RuleNode numexpr1 = getNumexprType((RuleNode)stmt.getChildren().get(3), varTypes);
					
					if(!isTypeCompatibleAssign(idType, numexpr1)) {
						throw new TigerParseException("Type of expression does not match type of iterating variable", getLeftmostLeaf((RuleNode)stmt.getChildren().get(3)));
					}
					
					RuleNode numexpr2 = getNumexprType((RuleNode)stmt.getChildren().get(5), varTypes);
					
					if(!isTypeCompatibleAssign(idType, numexpr2)) {
						throw new TigerParseException("Type of expression does not match type of iterating variable", getLeftmostLeaf((RuleNode)stmt.getChildren().get(5)));
					}
					
					analyzeStatements((RuleNode)stmt.getChildren().get(7), varTypes, true);
					
					break;
				case BREAK:
					if(!insideLoop) {
						throw new TigerParseException("Illegal break, not inside any loops", firstLeaf.getToken());
					}
					break;
				default:
					throw new TigerParseException("Unexpected statement", firstLeaf.getToken());
			}
		}
	}
	
	private void analyzeBoolexpr(RuleNode boolexpr, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(boolexpr.getValue(), TigerProductionRule.BOOLEXPR);
		
		RuleNode child = (RuleNode)boolexpr.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.BOOLEXPR) {
			analyzeBoolexpr(child, varTypes);
			analyzeClause((RuleNode)boolexpr.getChildren().get(2), varTypes);
		} else {
			analyzeClause(child, varTypes);
		}
	}
	
	private void analyzeClause(RuleNode clause, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		ensureValue(clause.getValue(), TigerProductionRule.CLAUSE);
		
		RuleNode child = (RuleNode)clause.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.CLAUSE) {
			analyzeClause(child, varTypes);
			analyzePred((RuleNode)clause.getChildren().get(2), varTypes);
		} else {
			analyzePred(child, varTypes);
		}
	}
	
	private void analyzePred(RuleNode pred, HashMap<String, RuleNode> varTypes) throws TigerParseException {
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
	
	private RuleNode getNumexprType(RuleNode numexpr, HashMap<String, RuleNode> varTypes) throws TigerParseException {
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
	
	private RuleNode getTermType(RuleNode term, HashMap<String, RuleNode> varTypes) throws TigerParseException {
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
	
	private RuleNode getFactorType(RuleNode factor, HashMap<String, RuleNode> varTypes) throws TigerParseException {
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
	
	private RuleNode getIdOptOffsetType(RuleNode rule, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		LeafNode id = (LeafNode)rule.getChildren().get(0);
		
		RuleNode idType = symbolTable.getVariables().get(id.getToken().getToken());
		
		if(idType == null) {
			throw new TigerParseException("Undeclared variable", id.getToken());
		}
		
		if(rule.getChildren().size() == 1) {
			return idType;
		} else {
			LeafNode lbracket, rbracket;
			RuleNode numexpr;
			
			if(rule.getChildren().get(1) instanceof RuleNode) { // optoffset rule
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
	
	private void analyzeFunctions() throws TigerParseException {
		analyzeFunction((RuleNode)((RuleNode)ast.getChildren().get(1)).getChildren().get(2));
	}
	
	private void analyzeFunction(RuleNode funcdecls) throws TigerParseException {
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		Pair<RuleNode, List<Pair<String, RuleNode>>> funcInfo = symbolTable.getFunctions().get(((LeafNode)funcdecl.getChildren().get(1)).getToken().getToken());
		if(!analyzeFunctionStmts(funcInfo, (RuleNode)funcdecl.getChildren().get(7)) && funcInfo.getKey() != null) {
			throw new TigerParseException("No return statement found", ((LeafNode)funcdecl.getChildren().get(8)).getToken());
		}
		analyzeFunction((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private boolean analyzeFunctionStmts(Pair<RuleNode, List<Pair<String, RuleNode>>> funcInfo, RuleNode stmts) throws TigerParseException {
		if(funcInfo == null) {
			throw new IllegalStateException("funcInfo is null somehow...");
		}
		
		HashMap<String, RuleNode> funcVarTypes = new HashMap<>();
		funcVarTypes.putAll(symbolTable.getVariables());
		funcInfo.getValue().forEach((Pair<String, RuleNode> p) -> funcVarTypes.put(p.getKey(), p.getValue()));
		
		RuleNode stmt = (RuleNode)((RuleNode)stmts.getChildren().get(0)).getChildren().get(0);
		Node first = stmt.getChildren().get(0);
		
		boolean doesReturn = false;
		
		if(first instanceof LeafNode && ((LeafNode)first).getToken().getTokenClass() == TigerTokenClass.RETURN) {
			RuleNode returnType = getNumexprType((RuleNode)stmt.getChildren().get(1), funcVarTypes);
			if(funcInfo.getKey() == null || !isTypeCompatibleAssign((funcInfo.getKey()), returnType)) {
				throw new TigerParseException("Type of returned expression does not match return type", ((LeafNode)first).getToken());
			}
			doesReturn = true;
		} else {
			analyzeStatement(stmt, funcVarTypes, false);
		}
		
		if(stmts.getChildren().size() > 1) {
			doesReturn |= analyzeFunctionStmts(funcInfo, (RuleNode)stmts.getChildren().get(1));
		}
		
		return doesReturn;
	}
}
