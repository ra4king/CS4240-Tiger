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
		analyzeFunction((RuleNode)((RuleNode)ast.getChildren().get(1)).getChildren().get(2));
	}
	
	private void analyzeFunction(RuleNode funcdecls) throws TigerParseException {
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		Pair<TigerType, List<Pair<String, TigerType>>> funcInfo = symbolTable.getFunctions().get(((LeafNode)funcdecl.getChildren().get(1)).getToken().getToken());
		if(!analyzeFunctionStmts(funcInfo, (RuleNode)funcdecl.getChildren().get(7)) && funcInfo.getKey() != null) {
			throw new TigerParseException("Not all code paths return", ((LeafNode)funcdecl.getChildren().get(8)).getToken());
		}
		analyzeFunction((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private boolean analyzeFunctionStmts(Pair<TigerType, List<Pair<String, TigerType>>> funcInfo, RuleNode stmts) throws TigerParseException {
		if(funcInfo == null) {
			throw new IllegalStateException("funcInfo is null somehow...");
		}
		
		HashMap<String, TigerType> funcVarTypes = new HashMap<>();
		funcVarTypes.putAll(symbolTable.getVariables());
		funcInfo.getValue().forEach((Pair<String, TigerType> p) -> funcVarTypes.put(p.getKey(), p.getValue()));
		
		RuleNode stmt = (RuleNode)((RuleNode)stmts.getChildren().get(0)).getChildren().get(0);
		Node first = stmt.getChildren().get(0);
		
		boolean doesReturn = false;
		
		if(first instanceof LeafNode && ((LeafNode)first).getToken().getTokenClass() == TigerTokenClass.RETURN) {
			TigerType returnType = getNumexprType((RuleNode)stmt.getChildren().get(1), funcVarTypes);
			if(funcInfo.getKey() == null || !isTypeCompatibleAssign((funcInfo.getKey()), returnType)) {
				throw new TigerParseException("Type of returned expression does not match return type", ((LeafNode)first).getToken());
			}
			doesReturn = true;
		} else {
			analyzeStatement(stmt, funcVarTypes, funcInfo.getKey(), false);
		}
		
		if(stmts.getChildren().size() > 1) {
			doesReturn |= analyzeFunctionStmts(funcInfo, (RuleNode)stmts.getChildren().get(1));
		}
		
		return doesReturn;
	}
	
	private void analyzeProgramStatements() throws TigerParseException {
		analyzeStatements((RuleNode)ast.getChildren().get(3), symbolTable.getVariables(), null, false);
	}
	
	private void analyzeStatements(RuleNode stmts, HashMap<String, TigerType> varTypes, TigerType returnValue, boolean insideLoop) throws TigerParseException {
		ensureValue(stmts.getValue(), TigerProductionRule.STMTS);
		
		if(stmts.getChildren().size() == 0) {
			return;
		}
		
		RuleNode fullstmt = (RuleNode)stmts.getChildren().get(0);
		ensureValue(fullstmt.getValue(), TigerProductionRule.FULLSTMT);
		analyzeStatement((RuleNode)fullstmt.getChildren().get(0), varTypes, returnValue, insideLoop);
		
		if(stmts.getChildren().size() > 1) {
			analyzeStatements((RuleNode)stmts.getChildren().get(1), varTypes, returnValue, insideLoop);
		}
	}
	
	private void analyzeStatement(RuleNode stmt, HashMap<String, TigerType> varTypes, TigerType returnType, boolean insideLoop) throws TigerParseException {
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
					analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, returnType, insideLoop);
					
					if(((LeafNode)stmt.getChildren().get(4)).getToken().getTokenClass() == TigerTokenClass.ELSE) {
						analyzeStatements((RuleNode)stmt.getChildren().get(5), varTypes, returnType, insideLoop);
					}
					
					break;
				case WHILE:
					analyzeBoolexpr((RuleNode)stmt.getChildren().get(1), varTypes);
					analyzeStatements((RuleNode)stmt.getChildren().get(3), varTypes, returnType, true);
					
					break;
				case FOR:
					TigerType idType = varTypes.get(((LeafNode)stmt.getChildren().get(1)).getToken().getToken());
					if(idType == null) {
						throw new TigerParseException("Undeclared variable", firstLeaf.getToken());
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
					
					analyzeStatements((RuleNode)stmt.getChildren().get(7), varTypes, returnType, true);
					
					break;
				case BREAK:
					if(!insideLoop) {
						throw new TigerParseException("Illegal break, not inside any loops", firstLeaf.getToken());
					}
					break;
				case RETURN:
					if(returnType == null) {
						throw new TigerParseException("Illegal return statement", firstLeaf.getToken());
					}
					
					if(!isTypeCompatibleAssign(returnType, returnType)) {
						throw new TigerParseException("Type of returned expression does not match return type", ((LeafNode)first).getToken());
					}
					
					break;
				default:
					throw new TigerParseException("Unexpected statement", firstLeaf.getToken());
			}
		}
	}
}
