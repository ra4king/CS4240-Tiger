package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.analyzer.TigerTypeAnalyzer.*;
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
	
	private void analyzeFunctions() throws TigerParseException {
		if(((RuleNode)ast.getChildren().get(1)).getChildren().size() > 0) {
			analyzeFunction((RuleNode)((RuleNode)ast.getChildren().get(1)).getChildren().get(2));
		}
	}
	
	private void analyzeFunction(RuleNode funcdecls) throws TigerParseException {
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		Pair<TigerType, List<Pair<String, TigerType>>> funcInfo = symbolTable.getFunctions().get(((LeafNode)funcdecl.getChildren().get(1)).getToken().getToken());
		
		HashMap<String, TigerType> funcVarTypes = new HashMap<>();
		funcVarTypes.putAll(symbolTable.getVariables());
		funcInfo.getValue().forEach((Pair<String, TigerType> p) -> funcVarTypes.put(p.getKey(), p.getValue()));
		
		if(!analyzeStatements((RuleNode)funcdecl.getChildren().get(7), funcVarTypes, funcInfo.getKey(), false).getKey() && funcInfo.getKey() != null) {
			throw new TigerParseException("Not all code paths return", ((LeafNode)funcdecl.getChildren().get(8)).getToken());
		}
		analyzeFunction((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private void analyzeProgramStatements() throws TigerParseException {
		analyzeStatements((RuleNode)ast.getChildren().get(3), symbolTable.getVariables(), null, false);
	}
	
	private Pair<Boolean, Boolean> analyzeStatements(RuleNode stmts, HashMap<String, TigerType> varTypes, TigerType returnValue, boolean insideLoop) throws TigerParseException {
		ensureValue(stmts.getValue(), TigerProductionRule.STMTS);
		
		if(stmts.getChildren().size() == 0) {
			return new Pair<>(false, false);
		}
		
		RuleNode fullstmt = (RuleNode)stmts.getChildren().get(0);
		ensureValue(fullstmt.getValue(), TigerProductionRule.FULLSTMT);
		Pair<Boolean, Boolean> doesReturn = analyzeStatement((RuleNode)fullstmt.getChildren().get(0), varTypes, returnValue, insideLoop);
		
		if(stmts.getChildren().size() > 1) {
			if(doesReturn.getKey() || doesReturn.getValue()) {
				throw new TigerParseException("Dead code", getLeftmostLeaf((RuleNode)stmts.getChildren().get(1)));
			} else {
				doesReturn = analyzeStatements((RuleNode)stmts.getChildren().get(1), varTypes, returnValue, insideLoop);
			}
		}
		
		return doesReturn;
	}
	
	private Pair<Boolean, Boolean> analyzeStatement(RuleNode stmt, HashMap<String, TigerType> varTypes, TigerType returnType, boolean insideLoop) throws TigerParseException {
		ensureValue(stmt.getValue(), TigerProductionRule.STMT);
		
		Node first = stmt.getChildren().get(0);
		if(first instanceof RuleNode) {
			RuleNode firstRule = (RuleNode)first;
			
			switch(firstRule.getValue()) {
				case LVALUE:
					TigerType lvalueType = getIdOptOffsetType((RuleNode)stmt.getChildren().get(0), varTypes);
					TigerType numexprType = getNumexprType((RuleNode)stmt.getChildren().get(2), varTypes);
					
					if(!isTypeCompatibleAssign(lvalueType, numexprType)) {
						throw new TigerParseException("Incompatible types", getLeftmostLeaf((RuleNode)stmt.getChildren().get(2)));
					}
					
					break;
				case OPTSTORE:
					TigerToken functionToken = ((LeafNode)stmt.getChildren().get(1)).getToken();
					Pair<TigerType, List<Pair<String, TigerType>>> function = symbolTable.getFunctions().get(functionToken.getToken());
					
					RuleNode optstore = (RuleNode)stmt.getChildren().get(0);
					if(optstore.getChildren().size() > 0) {
						lvalueType = getIdOptOffsetType((RuleNode)optstore.getChildren().get(0), varTypes);
						
						if(function.getKey() == null) {
							throw new TigerParseException("Function does not return a value", functionToken);
						}
						
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
						
						for(Pair<String, TigerType> arg : function.getValue()) {
							if(neexprs == null) {
								throw new TigerParseException("Argument mismatch", functionToken);
							}
							
							RuleNode numexpr = (RuleNode)neexprs.getChildren().get(0);
							TigerType argType = getNumexprType(numexpr, varTypes);
							
							if(!isTypeCompatibleAssign(arg.getValue(), argType)) {
								throw new TigerParseException("Incompatible types", getLeftmostLeaf(numexpr));
							}
							
							if(neexprs.getChildren().size() == 3) {
								neexprs = (RuleNode)neexprs.getChildren().get(2);
							} else {
								neexprs = null;
							}
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
					Pair<Boolean, Boolean> doesReturn = analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, returnType, insideLoop);
					
					if(((LeafNode)stmt.getChildren().get(4)).getToken().getTokenClass() == TigerTokenClass.ELSE) {
						Pair<Boolean, Boolean> subStmtReturns = analyzeStatements((RuleNode)stmt.getChildren().get(5), varTypes, returnType, insideLoop);
						doesReturn.set(doesReturn.getKey() & subStmtReturns.getKey(), doesReturn.getValue() & subStmtReturns.getValue());
					} else {
						doesReturn = new Pair<>(false, false);
					}
					
					return doesReturn;
				case WHILE:
					analyzeBoolexpr((RuleNode)stmt.getChildren().get(1), varTypes);
					Pair<Boolean, Boolean> bodyReturns = analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, returnType, true);
					bodyReturns.setValue(false);
					return bodyReturns;
				case FOR:
					TigerType idType = varTypes.get(((LeafNode)stmt.getChildren().get(1)).getToken().getToken());
					if(idType == null) {
						throw new TigerParseException("Undeclared variable", ((LeafNode)stmt.getChildren().get(1)).getToken());
					}
					
					if(!idType.equals(TigerType.INT_TYPE)) {
						throw new TigerParseException("Iterating variable must be of integer type", ((LeafNode)stmt.getChildren().get(1)).getToken());
					}
					
					TigerType numexpr1 = getNumexprType((RuleNode)stmt.getChildren().get(3), varTypes);
					
					if(!isTypeCompatibleAssign(idType, numexpr1)) {
						throw new TigerParseException("Type of expression does not match type of iterating variable", getLeftmostLeaf((RuleNode)stmt.getChildren().get(3)));
					}
					
					TigerType numexpr2 = getNumexprType((RuleNode)stmt.getChildren().get(5), varTypes);
					
					if(!isTypeCompatibleAssign(idType, numexpr2)) {
						throw new TigerParseException("Type of expression does not match type of iterating variable", getLeftmostLeaf((RuleNode)stmt.getChildren().get(5)));
					}
					
					bodyReturns = analyzeStatements((RuleNode)stmt.getChildren().get(7), varTypes, returnType, true);
					bodyReturns.setValue(false);
					return bodyReturns;
				case BREAK:
					if(!insideLoop) {
						throw new TigerParseException("Illegal break, not inside any loops", firstLeaf.getToken());
					}
					return new Pair<>(false, true);
				case RETURN:
					if(returnType == null) {
						throw new TigerParseException("Illegal return statement", firstLeaf.getToken());
					}
					
					TigerType type = getNumexprType((RuleNode)stmt.getChildren().get(1), varTypes);
					
					if(!isTypeCompatibleAssign(returnType, type)) {
						throw new TigerParseException("Type of returned expression does not match return type", getLeftmostLeaf((RuleNode)stmt.getChildren().get(1)));
					}
					
					return new Pair<>(true, false);
				default:
					throw new TigerParseException("Unexpected statement", firstLeaf.getToken());
			}
		}
		
		return new Pair<>(false, false);
	}
}
