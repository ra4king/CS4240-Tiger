package edu.cs4240.tiger.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import edu.cs4240.tiger.analyzer.TigerType;
import edu.cs4240.tiger.analyzer.TigerType.BaseType;
import edu.cs4240.tiger.analyzer.TigerType.TigerArrayType;
import edu.cs4240.tiger.analyzer.TigerTypeAnalyzer;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerSymbol;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.parser.node.LeafNode;
import edu.cs4240.tiger.parser.node.Node;
import edu.cs4240.tiger.parser.node.RuleNode;
import edu.cs4240.tiger.util.Pair;
import edu.cs4240.tiger.util.Utils;

/**
 * @author Roi Atalla
 */
public class TigerSemanticallyCorrectGenerator {
	public static Node generate(Random rng, int limit) {
		RuleNode program = new RuleNode(TigerProductionRule.PROGRAM);
		program.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LET)));
		
		HashMap<String, TigerType> varTypes = new HashMap<>();
		HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> funcTypes = new HashMap<>();
		program.getChildren().add(generateDeclseg(rng, limit, varTypes, funcTypes));
		
		program.getChildren().add(new LeafNode(tokenify(TigerTokenClass.IN)));
		
		program.getChildren().add(generateStatements(rng, limit, null, false, varTypes, funcTypes).getKey());
		
		program.getChildren().add(new LeafNode(tokenify(TigerTokenClass.END)));
		
		return program;
	}
	
	private static Node generateDeclseg(Random rng, int limit, HashMap<String, TigerType> varTypes, HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> funcTypes) {
		RuleNode declsegNode = new RuleNode(TigerProductionRule.DECLSEG);
		
		HashMap<String, TigerType> typeAliases = new HashMap<>();
		declsegNode.getChildren().add(generateTypes(rng, limit - 1, typeAliases));
		declsegNode.getChildren().add(generateVars(rng, limit - 1, typeAliases, varTypes));
		declsegNode.getChildren().add(generateFuncs(rng, limit - 1, typeAliases, varTypes, funcTypes));
		
		return declsegNode;
	}
	
	private static Node generateTypes(Random rng, int limit, HashMap<String, TigerType> typeAliases) {
		RuleNode typedeclsNode = new RuleNode(TigerProductionRule.TYPEDECLS);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			RuleNode typedeclNode = new RuleNode(TigerProductionRule.TYPEDECL);
			
			typedeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.TYPE_)));
			String id = generateID(rng, typeAliases.keySet());
			typedeclNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
			typedeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ASSIGN)));
			Pair<RuleNode, TigerType> typePair = generateType(rng, typeAliases);
			typedeclNode.getChildren().add(typePair.getKey());
			typedeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.SEMICOLON)));
			
			typeAliases.put(id, typePair.getValue());
			
			typedeclsNode.getChildren().add(typedeclNode);
			typedeclsNode.getChildren().add(generateTypes(rng, limit - 1, typeAliases));
		}
		
		return typedeclsNode;
	}
	
	private static Node generateVars(Random rng, int limit, HashMap<String, TigerType> typeAliases, HashMap<String, TigerType> varTypes) {
		RuleNode vardeclsNode = new RuleNode(TigerProductionRule.VARDECLS);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			RuleNode vardeclNode = new RuleNode(TigerProductionRule.VARDECL);
			
			Pair<RuleNode, TigerType> typePair = generateType(rng, typeAliases);
			
			vardeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.VAR)));
			vardeclNode.getChildren().add(generateIDs(rng, limit - 1, typePair.getValue(), varTypes));
			vardeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COLON)));
			
			vardeclNode.getChildren().add(typePair.getKey());
			
			RuleNode optinitNode = new RuleNode(TigerProductionRule.OPTINIT);
			if(typePair.getValue().baseType != BaseType.ARRAY && notSoFairBoolean(rng)) {
				optinitNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ASSIGN)));
				
				RuleNode constNode = new RuleNode(TigerProductionRule.CONST);
				
				if(typePair.getValue().baseType == BaseType.INT) {
					constNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.INTLIT, GeneratorUtils.generateIntlit(rng), "", 0, 0)));
				} else if(typePair.getValue().baseType == BaseType.FLOAT) {
					if(rng.nextBoolean()) {
						constNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.INTLIT, GeneratorUtils.generateIntlit(rng), "", 0, 0)));
					} else {
						constNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.FLOATLIT, GeneratorUtils.generateFloatlit(rng), "", 0, 0)));
					}
				} else {
					throw new IllegalStateException("Wat: " + typePair.getValue());
				}
				
				optinitNode.getChildren().add(constNode);
			}
			
			vardeclNode.getChildren().add(optinitNode);
			vardeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.SEMICOLON)));
			
			vardeclsNode.getChildren().add(vardeclNode);
			vardeclsNode.getChildren().add(generateVars(rng, limit - 1, typeAliases, varTypes));
		}
		
		return vardeclsNode;
	}
	
	private static Node generateIDs(Random rng, int limit, TigerType type, HashMap<String, TigerType> varTypes) {
		RuleNode idsNode = new RuleNode(TigerProductionRule.IDS);
		
		String id = generateID(rng, varTypes.keySet());
		idsNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
		
		varTypes.put(id, type);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			idsNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COMMA)));
			idsNode.getChildren().add(generateIDs(rng, limit - 1, type, varTypes));
		}
		
		return idsNode;
	}
	
	private static Node generateFuncs(Random rng, int limit, HashMap<String, TigerType> typeAliases, HashMap<String, TigerType> varTypes, HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> funcTypes) {
		RuleNode funcdeclsNode = new RuleNode(TigerProductionRule.FUNCDECLS);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			RuleNode funcdeclNode = new RuleNode(TigerProductionRule.FUNCDECL);
			
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.FUNC)));
			String id = generateID(rng, funcTypes.keySet());
			funcdeclNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LPAREN)));
			
			Pair<RuleNode, TigerType> typePair = null;
			if(rng.nextBoolean()) {
				typePair = generateExistingType(rng, typeAliases, varTypes);
//				boolean chooseAgain = true;
//				do {
//					typePair = generateType(rng, typeAliases);
//					
//					if(typePair.getRule().type == Type.ARRAY) {
//						for(TigerType type : varTypes.values()) {
//							if(typePair.getRule().equals(type)) {
//								chooseAgain = false;
//								break;
//							}
//						}
//					} else {
//						chooseAgain = false;
//					}
//				} while(chooseAgain);
			}
			
			List<Pair<String, TigerType>> funcParams = new ArrayList<>();
			funcdeclNode.getChildren().add(new RuleNode(TigerProductionRule.PARAMS, generateFuncParams(rng, limit - 1, typeAliases, varTypes, funcParams)));
			
			funcTypes.put(id, new Pair<>(typePair == null ? null : typePair.getValue(), funcParams));
			
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RPAREN)));
			
			RuleNode optrettypeNode = new RuleNode(TigerProductionRule.OPTRETTYPE);
			if(typePair != null) {
				optrettypeNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COLON)));
				optrettypeNode.getChildren().add(typePair.getKey());
			}
			
			funcdeclNode.getChildren().add(optrettypeNode);
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.BEGIN)));
			
			HashMap<String, TigerType> funcVarTypes = new HashMap<>(varTypes);
			for(Pair<String, TigerType> pair : funcParams) {
				funcVarTypes.put(pair.getKey(), pair.getValue());
			}
			
			funcdeclNode.getChildren().add(generateStatements(rng, limit - 1, typePair == null ? null : typePair.getValue(), false, funcVarTypes, funcTypes).getKey());
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.END)));
			funcdeclNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.SEMICOLON)));
			
			funcdeclsNode.getChildren().add(funcdeclNode);
			funcdeclsNode.getChildren().add(generateFuncs(rng, limit - 1, typeAliases, varTypes, funcTypes));
		}
		
		return funcdeclsNode;
	}
	
	private static Node generateFuncParams(Random rng, int limit, HashMap<String, TigerType> typeAliases, HashMap<String, TigerType> varTypes, List<Pair<String, TigerType>> funcParams) {
		RuleNode neparamsNode = new RuleNode(TigerProductionRule.NEPARAMS);
		
		RuleNode paramNode = new RuleNode(TigerProductionRule.PARAM);
		String id = generateID(rng, funcParams);
		paramNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
		
		Pair<RuleNode, TigerType> typePair = generateExistingType(rng, typeAliases, varTypes);
		
		paramNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COLON)));
		paramNode.getChildren().add(typePair.getKey());
		
		funcParams.add(new Pair<>(id, typePair.getValue()));
		
		neparamsNode.getChildren().add(paramNode);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			neparamsNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COMMA)));
			neparamsNode.getChildren().add(generateFuncParams(rng, limit - 1, typeAliases, varTypes, funcParams));
		}
		
		return neparamsNode;
	}
	
	private static Pair<RuleNode, TigerType> generateExistingType(Random rng, HashMap<String, TigerType> typeAliases, HashMap<String, TigerType> varTypes) {
		do {
			Pair<RuleNode, TigerType> typePair = generateType(rng, typeAliases);
			
			if(typePair.getValue().baseType == BaseType.ARRAY) {
				for(TigerType type : varTypes.values()) {
					if(typePair.getValue().equals(type)) {
						return typePair;
					}
				}
			} else {
				return typePair;
			}
		} while(true);
	}
	
	// Pair<Node, Pair<Returns?, Breaks?>>
	private static Pair<Node, Pair<Boolean, Boolean>> generateStatements(Random rng, int limit, TigerType returnType, boolean insideLoop, HashMap<String, TigerType> varTypes, HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> funcTypes) {
		RuleNode stmtsNode = new RuleNode(TigerProductionRule.STMTS);
		
		RuleNode fullstmtNode = new RuleNode(TigerProductionRule.FULLSTMT);
		
		Pair<Boolean, Boolean> returnBreakResult = new Pair<>(false, false);
		
		RuleNode stmtNode;
		do {
			stmtNode = null;
			
			List<TigerSymbol> chosenStmt = TigerProductionRule.STMT.productions.get(rng.nextInt(TigerProductionRule.STMT.productions.size()));
			if(chosenStmt.get(0) instanceof TigerProductionRule) {
				switch((TigerProductionRule)chosenStmt.get(0)) {
					case LVALUE: {
						if(varTypes.size() == 0) {
							continue;
						}
						
						stmtNode = new RuleNode(TigerProductionRule.STMT);
						
						String id = chooseRandomKey(rng, varTypes.keySet());
						TigerType type = varTypes.get(id);
						
						Pair<RuleNode, TigerType> typePair = generateLValue(rng, limit, id, type, varTypes);
						type = typePair.getValue();
						
						stmtNode.getChildren().add(typePair.getKey());
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ASSIGN)));
						
						Node numexprNode = generateNumexpr(rng, limit, type, varTypes);
						if(numexprNode == null) {
							stmtNode = null;
							continue;
						}
						
						stmtNode.getChildren().add(numexprNode);
						break;
					}
					case OPTSTORE: {
						if(funcTypes.size() == 0) {
							continue;
						}
						
						stmtNode = new RuleNode(TigerProductionRule.STMT);
						
						TigerType type = null;
						
						RuleNode optstoreNode = new RuleNode(TigerProductionRule.OPTSTORE);
						if(limit > 0 && varTypes.size() > 0 && notSoFairBoolean(rng)) {
							String id = chooseRandomKey(rng, varTypes.keySet());
							type = varTypes.get(id);
							
							Pair<RuleNode, TigerType> typePair = generateLValue(rng, limit - 1, id, type, varTypes);
							type = typePair.getValue();
							
							optstoreNode.getChildren().add(typePair.getKey());
							optstoreNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ASSIGN)));
						}
						
						stmtNode.getChildren().add(optstoreNode);
						
						String id = chooseRandomKey(rng, funcTypes.keySet());
						Pair<TigerType, List<Pair<String, TigerType>>> function;
						int tryCount = 4;
						do {
							function = funcTypes.get(id);
							if(tryCount-- == 0) {
								stmtNode = null;
								break;
							}
						} while(type != null && (function.getKey() == null ||
						                           !TigerTypeAnalyzer.isTypeCompatibleAssign(type, function.getKey())));
						
						if(stmtNode == null) {
							continue;
						}
						
						stmtNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LPAREN)));
						
						RuleNode numexprsNode = new RuleNode(TigerProductionRule.NUMEXPRS);
						RuleNode neexprsNode = null;
						for(Pair<String, TigerType> param : function.getValue()) {
							if(neexprsNode != null) {
								neexprsNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.COMMA)));
								RuleNode temp = new RuleNode(TigerProductionRule.NEEXPRS);
								neexprsNode.getChildren().add(temp);
								neexprsNode = temp;
							} else {
								neexprsNode = new RuleNode(TigerProductionRule.NEEXPRS);
								numexprsNode.getChildren().add(neexprsNode);
							}
							
							Node numexprNode = generateNumexpr(rng, limit, param.getValue(), varTypes);
							if(numexprNode == null) {
								stmtNode = null;
								break;
							}
							
							neexprsNode.getChildren().add(numexprNode);
						}
						
						if(stmtNode == null) {
							continue;
						}
						
						stmtNode.getChildren().add(numexprsNode);
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RPAREN)));
						
						break;
					}
					default:
						throw new IllegalStateException("WAT?! " + chosenStmt.get(0));
				}
			} else {
				switch((TigerTokenClass)chosenStmt.get(0)) {
					case IF: {
						stmtNode = new RuleNode(TigerProductionRule.STMT);
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.IF)));
						stmtNode.getChildren().add(generateBoolexpr(rng, limit - 1, varTypes));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.THEN)));
						
						Pair<Node, Pair<Boolean, Boolean>> stmtsPair = generateStatements(rng, limit - 1, returnType, insideLoop, varTypes, funcTypes);
						stmtNode.getChildren().add(stmtsPair.getKey());
						returnBreakResult.set(stmtsPair.getValue().getKey(), stmtsPair.getValue().getValue());
						
						if(rng.nextBoolean()) {
							stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ELSE)));
							stmtsPair = generateStatements(rng, limit - 1, returnType, insideLoop, varTypes, funcTypes);
							stmtNode.getChildren().add(stmtsPair.getKey());
							returnBreakResult.set(returnBreakResult.getKey() && stmtsPair.getValue().getKey(), returnBreakResult.getValue() && stmtsPair.getValue().getValue());
						} else {
							returnBreakResult.set(false, false);
						}
						
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ENDIF)));
						break;
					}
					case WHILE: {
						stmtNode = new RuleNode(TigerProductionRule.STMT);
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.WHILE)));
						stmtNode.getChildren().add(generateBoolexpr(rng, limit - 1, varTypes));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.DO)));
						
						Pair<Node, Pair<Boolean, Boolean>> stmtsPair = generateStatements(rng, limit - 1, returnType, true, varTypes, funcTypes);
						stmtNode.getChildren().add(stmtsPair.getKey());
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ENDDO)));
						
						returnBreakResult.setKey(stmtsPair.getValue().getKey());
						break;
					}
					case FOR:
						Set<String> ints = varTypes.keySet().stream().filter(s -> varTypes.get(s).baseType == BaseType.INT).collect(Collectors.toSet());
						if(ints.size() == 0) {
							continue;
						}
						
						stmtNode = new RuleNode(TigerProductionRule.STMT);
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.FOR)));
						stmtNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, chooseRandomKey(rng, ints), "", 0, 0)));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ASSIGN)));
						stmtNode.getChildren().add(generateNumexpr(rng, limit - 1, TigerType.INT_TYPE, varTypes));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.TO)));
						stmtNode.getChildren().add(generateNumexpr(rng, limit - 1, TigerType.INT_TYPE, varTypes));
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.DO)));
						
						Pair<Node, Pair<Boolean, Boolean>> stmtsPair = generateStatements(rng, limit - 1, returnType, true, varTypes, funcTypes);
						stmtNode.getChildren().add(stmtsPair.getKey());
						stmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ENDDO)));
						
						returnBreakResult.setKey(stmtsPair.getValue().getKey());
						break;
					case BREAK:
						if(!insideLoop) {
							continue;
						}
						
						returnBreakResult.setValue(true);
						stmtNode = new RuleNode(TigerProductionRule.STMT, new LeafNode(tokenify(TigerTokenClass.BREAK)));
						break;
					case RETURN:
						if(returnType == null) {
							continue;
						}
						
						returnBreakResult.setKey(true);
						stmtNode = new RuleNode(TigerProductionRule.STMT, new LeafNode(tokenify(TigerTokenClass.RETURN)));
						stmtNode.getChildren().add(generateNumexpr(rng, limit - 1, returnType, varTypes));
						break;
				}
			}
		} while(stmtNode == null);
		
		fullstmtNode.getChildren().add(stmtNode);
		fullstmtNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.SEMICOLON)));
		
		stmtsNode.getChildren().add(fullstmtNode);
		
		if(!(returnBreakResult.getKey() || returnBreakResult.getValue()) && ((returnType != null && !insideLoop) || (limit > 0 && notSoFairBoolean(rng)))) {
			Pair<Node, Pair<Boolean, Boolean>> stmtsPair = generateStatements(rng, limit - 1, returnType, insideLoop, varTypes, funcTypes);
			stmtsNode.getChildren().add(stmtsPair.getKey());
			returnBreakResult = stmtsPair.getValue();
		}
		
		return new Pair<>(stmtsNode, returnBreakResult);
	}
	
	private static Pair<RuleNode, TigerType> generateLValue(Random rng, int limit, String id, TigerType type, HashMap<String, TigerType> varTypes) {
		RuleNode lvalueNode = new RuleNode(TigerProductionRule.LVALUE);
		lvalueNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
		
		RuleNode optoffset = new RuleNode(TigerProductionRule.OPTOFFSET);
		if(type.baseType == BaseType.ARRAY && limit > 0 && notSoFairBoolean(rng)) {
			optoffset.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LBRACKET)));
			optoffset.getChildren().add(generateNumexpr(rng, limit - 1, TigerType.INT_TYPE, varTypes));
			optoffset.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RBRACKET)));
			type = ((TigerArrayType)type).subType;
		}
		
		lvalueNode.getChildren().add(optoffset);
		return new Pair<>(lvalueNode, type);
	}
	
	private static Node generateNumexpr(Random rng, int limit, TigerType exprType, HashMap<String, TigerType> varTypes) {
		RuleNode numexprNode = new RuleNode(TigerProductionRule.NUMEXPR);
		
		if(limit > 0 && TigerType.isNumericType(exprType) && notSoFairBoolean(rng)) {
			Node subNumexprNode = generateNumexpr(rng, limit / 2, exprType, varTypes);
			if(subNumexprNode == null) {
				return null;
			}
			
			numexprNode.getChildren().add(subNumexprNode);
			
			if(rng.nextBoolean()) {
				numexprNode.getChildren().add(new RuleNode(TigerProductionRule.LINOP, new LeafNode(tokenify(TigerTokenClass.PLUS))));
			} else {
				numexprNode.getChildren().add(new RuleNode(TigerProductionRule.LINOP, new LeafNode(tokenify(TigerTokenClass.MINUS))));
			}
		}
		
		Node termNode = generateTerm(rng, limit / 2, exprType, varTypes);
		if(termNode == null) {
			return null;
		}
		
		numexprNode.getChildren().add(termNode);
		
		return numexprNode;
	}
	
	private static Node generateTerm(Random rng, int limit, TigerType exprType, HashMap<String, TigerType> varTypes) {
		RuleNode termNode = new RuleNode(TigerProductionRule.TERM);
		
		if(limit > 0 && TigerType.isNumericType(exprType) && notSoFairBoolean(rng)) {
			Node subTermNode = generateTerm(rng, limit / 2, exprType, varTypes);
			if(subTermNode == null) {
				return null;
			}
			
			termNode.getChildren().add(subTermNode);
			
			if(rng.nextBoolean()) {
				termNode.getChildren().add(new RuleNode(TigerProductionRule.NONLINOP, new LeafNode(tokenify(TigerTokenClass.STAR))));
			} else {
				termNode.getChildren().add(new RuleNode(TigerProductionRule.NONLINOP, new LeafNode(tokenify(TigerTokenClass.FWSLASH))));
			}
		}
		
		Node factorNode = generateFactor(rng, limit / 2, exprType, varTypes);
		if(factorNode == null) {
			return null;
		}
		
		termNode.getChildren().add(factorNode);
		
		return termNode;
	}
	
	private static Node generateFactor(Random rng, int limit, TigerType exprType, HashMap<String, TigerType> varTypes) {
		RuleNode factorNode = new RuleNode(TigerProductionRule.FACTOR);
		
		int tryCount = 4;
		do {
			tryCount--;
			
			List<TigerSymbol> chosenRule = TigerProductionRule.FACTOR.productions.get(rng.nextInt(TigerProductionRule.FACTOR.productions.size()));
			
			if(tryCount == -1 || chosenRule.get(0) == TigerProductionRule.CONST) {
				if(exprType.baseType == BaseType.INT) {
					LeafNode intlit = new LeafNode(new TigerToken(TigerTokenClass.INTLIT, GeneratorUtils.generateIntlit(rng), "", 0, 0));
					factorNode.getChildren().add(new RuleNode(TigerProductionRule.CONST, intlit));
				} else if(exprType.baseType == BaseType.FLOAT) {
					if(rng.nextBoolean()) {
						LeafNode intlit = new LeafNode(new TigerToken(TigerTokenClass.INTLIT, GeneratorUtils.generateIntlit(rng), "", 0, 0));
						factorNode.getChildren().add(new RuleNode(TigerProductionRule.CONST, intlit));
					} else {
						LeafNode intlit = new LeafNode(new TigerToken(TigerTokenClass.FLOATLIT, GeneratorUtils.generateFloatlit(rng), "", 0, 0));
						factorNode.getChildren().add(new RuleNode(TigerProductionRule.CONST, intlit));
					}
				} else {
					continue;
				}
			} else if(tryCount < -1 || chosenRule.get(0) == TigerTokenClass.ID) {
				Set<String> idsSet = new HashSet<>();
				for(String id : varTypes.keySet()) {
					TigerType idType = varTypes.get(id);
					
					if(TigerTypeAnalyzer.isTypeCompatibleAssign(exprType, idType)) {
						idsSet.add(id);
					} else if(idType.baseType == BaseType.ARRAY) {
						if(TigerTypeAnalyzer.isTypeCompatibleAssign(exprType, ((TigerArrayType)idType).subType)) {
							idsSet.add(id);
						}
					}
				}
				
				if(idsSet.size() == 0) {
					if(tryCount < -1) {
						//System.err.println("Reached trycount == " + tryCount + " with no ID found?!");
						return null;
					}
					
					continue;
				}
				
				String id = chooseRandomKey(rng, idsSet);
				factorNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, id, "", 0, 0)));
				
				if(!TigerTypeAnalyzer.isTypeCompatibleAssign(exprType, varTypes.get(id))) {
					factorNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LBRACKET)));
					factorNode.getChildren().add(generateNumexpr(rng, limit / 2, TigerType.INT_TYPE, varTypes));
					factorNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RBRACKET)));
				}
			} else if(limit > 0 && chosenRule.get(0) == TigerTokenClass.LPAREN) {
				factorNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LPAREN)));
				factorNode.getChildren().add(generateNumexpr(rng, limit / 2, exprType, varTypes));
				factorNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RPAREN)));
			} else {
				continue;
			}
			
			break;
		} while(true);
		
		return factorNode;
	}
	
	private static Node generateBoolexpr(Random rng, int limit, HashMap<String, TigerType> varTypes) {
		RuleNode boolexprNode = new RuleNode(TigerProductionRule.BOOLEXPR);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			boolexprNode.getChildren().add(generateBoolexpr(rng, limit / 2, varTypes));
			boolexprNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.PIPE)));
		}
		
		boolexprNode.getChildren().add(generateClause(rng, limit / 2, varTypes));
		
		return boolexprNode;
	}
	
	private static Node generateClause(Random rng, int limit, HashMap<String, TigerType> varTypes) {
		RuleNode clauseNode = new RuleNode(TigerProductionRule.CLAUSE);
		
		if(limit > 0 && notSoFairBoolean(rng)) {
			clauseNode.getChildren().add(generateClause(rng, limit / 2, varTypes));
			clauseNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.AMP)));
		}
		
		clauseNode.getChildren().add(generatePred(rng, limit / 2, varTypes));
		
		return clauseNode;
	}
	
	private static Node generatePred(Random rng, int limit, HashMap<String, TigerType> varTypes) {
		RuleNode predNode = new RuleNode(TigerProductionRule.PRED);
		
		TigerType type1 = rng.nextBoolean() ? TigerType.INT_TYPE : TigerType.FLOAT_TYPE;
		TigerType type2 = rng.nextBoolean() ? TigerType.INT_TYPE : TigerType.FLOAT_TYPE;
		
		predNode.getChildren().add(generateNumexpr(rng, limit / 2, type1, varTypes));
		TigerTokenClass chosenBoolop = (TigerTokenClass)TigerProductionRule.BOOLOP.productions.get(rng.nextInt(TigerProductionRule.BOOLOP.productions.size())).get(0);
		predNode.getChildren().add(new RuleNode(TigerProductionRule.BOOLOP, new LeafNode(tokenify(chosenBoolop))));
		predNode.getChildren().add(generateNumexpr(rng, limit / 2, type2, varTypes));
		
		return predNode;
	}
	
	private static String generateID(Random rng, Set<String> existingIDs) {
		String s;
		do {
			s = GeneratorUtils.generateID(rng);
		} while(existingIDs.contains(s));
		
		return s;
	}
	
	private static <V> String generateID(Random rng, List<Pair<String, V>> existingIDs) {
		String s;
		do {
			s = GeneratorUtils.generateID(rng);
		} while(Pair.containsKey(existingIDs, s));
		
		return s;
	}
	
	private static Pair<RuleNode, TigerType> generateType(Random rng, HashMap<String, TigerType> typeAliases) {
		RuleNode typeNode = new RuleNode(TigerProductionRule.TYPE);
		
		List<TigerSymbol> chosenType;
		do {
			chosenType = TigerProductionRule.TYPE.productions.get(rng.nextInt(TigerProductionRule.TYPE.productions.size()));
		} while(chosenType.get(0) == TigerTokenClass.ID && typeAliases.size() == 0);
		
		if(chosenType.get(0) == TigerTokenClass.ARRAY) {
			String size = GeneratorUtils.generateIntlit(rng);
			
			typeNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.ARRAY)));
			typeNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.LBRACKET)));
			typeNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.INTLIT, size, "", 0, 0)));
			typeNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.RBRACKET)));
			typeNode.getChildren().add(new LeafNode(tokenify(TigerTokenClass.OF)));
			
			Pair<RuleNode, TigerType> subTypeNode = generateType(rng, typeAliases);
			
			typeNode.getChildren().add(subTypeNode.getKey());
			
			return new Pair<>(typeNode, new TigerArrayType(subTypeNode.getValue(), Integer.parseInt(size)));
		} else if(chosenType.get(0) == TigerTokenClass.ID) {
			String idTypeName = chooseRandomKey(rng, typeAliases.keySet());
			TigerType idType = typeAliases.get(idTypeName);
			
			typeNode.getChildren().add(new LeafNode(new TigerToken(TigerTokenClass.ID, idTypeName, "", 0, 0)));
			return new Pair<>(typeNode, idType);
		}
		
		TigerToken token = tokenify((TigerTokenClass)chosenType.get(0));
		typeNode.getChildren().add(new LeafNode(token));
		return new Pair<>(typeNode, TigerType.getLiteralType(token));
	}
	
	private static <T> T chooseRandomKey(Random rng, Set<T> set) {
		int rdm = rng.nextInt(set.size());
		int count = 0;
		for(T t : set) {
			if(count++ == rdm) {
				return t;
			}
		}
		
		throw new IllegalStateException("WTF?");
	}
	
	private static TigerToken tokenify(TigerTokenClass symbol) {
		String specialToken = Utils.specialTokenClassesToString.get(symbol);
		return new TigerToken(symbol, specialToken == null ? symbol.toString().replace("_", "").toLowerCase() : specialToken, "", 0, 0);
	}
	
	private static boolean notSoFairBoolean(Random rng) {
		int tryCount = rng.nextInt(2) + 1;
		while(tryCount > 0 && !rng.nextBoolean()) {
			tryCount--;
		}
		
		return tryCount > 0;
	}
}
