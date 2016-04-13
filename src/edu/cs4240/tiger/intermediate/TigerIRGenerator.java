package edu.cs4240.tiger.intermediate;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import edu.cs4240.tiger.analyzer.TigerSymbolTable;
import edu.cs4240.tiger.analyzer.TigerType;
import edu.cs4240.tiger.analyzer.TigerType.BaseType;
import edu.cs4240.tiger.analyzer.TigerType.TigerArrayType;
import edu.cs4240.tiger.intermediate.TigerIROpcode.ParamType;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;
import edu.cs4240.tiger.parser.node.LeafNode;
import edu.cs4240.tiger.parser.node.Node;
import edu.cs4240.tiger.parser.node.RuleNode;
import edu.cs4240.tiger.util.Pair;

/**
 * @author Roi Atalla
 */
public class TigerIRGenerator {
	private RuleNode ast;
	private TigerSymbolTable symbolTable;
	
	public TigerIRGenerator(RuleNode ast, TigerSymbolTable symbolTable) {
		this.ast = ast;
		this.symbolTable = symbolTable;
	}
	
	public List<String> generateIR() {
		ArrayList<String> ir = new ArrayList<>();
		
		generateVariables(ir);
		generateFunctions(ir);
		generateMain(ir);
		
		return ir;
	}
	
	private void generateVariables(List<String> ir) {
		HashMap<String, Pair<TigerType, String>> variables = symbolTable.getVariables();
		for(String var : variables.keySet()) {
			Pair<TigerType, String> type = variables.get(var);
			
			if(type.getKey().baseType == BaseType.INT) {
				ir.add(".VARi " + var + " " + type.getValue());
			} else if(type.getKey().baseType == BaseType.FLOAT) {
				ir.add(".VARf " + var + " " + type.getValue());
			} else if(type.getKey().baseType == BaseType.ARRAY) {
				String instr = ".ARRAY";
				
				String sizes = "";
				for(TigerArrayType currType = (TigerArrayType)type.getKey(); ; ) {
					sizes += " " + currType.size;
					
					if(currType.subType.baseType == BaseType.INT) {
						instr += "i ";
						break;
					} else if(currType.subType.baseType == BaseType.FLOAT) {
						instr += "f ";
						break;
					} else {
						currType = (TigerArrayType)currType.subType;
					}
				}
				
				ir.add(instr + var + sizes);
			}
		}
	}
	
	private void generateFunctions(List<String> ir) {
		RuleNode declseg = (RuleNode)ast.getChildren().get(1);
		if(declseg.getChildren().size() < 3) {
			return;
		}
		
		RuleNode funcdecls = (RuleNode)declseg.getChildren().get(2);
		while(funcdecls.getChildren().size() > 0) {
			generateFunction((RuleNode)funcdecls.getChildren().get(0), ir);
			
			funcdecls = (RuleNode)funcdecls.getChildren().get(1);
		}
	}
	
	private void generateFunction(RuleNode ruleNode, List<String> ir) {
		ensureValue(ruleNode.getRule(), TigerProductionRule.FUNCDECL);
		
		String funcName = ((LeafNode)ruleNode.getChildren().get(1)).getToken().getTokenString();
		String params = "";
		
		RuleNode paramsNode = (RuleNode)ruleNode.getChildren().get(3);
		if(paramsNode.getChildren().size() > 0) {
			RuleNode neparamsNode = (RuleNode)paramsNode.getChildren().get(0);
			
			do {
				RuleNode paramNode = (RuleNode)neparamsNode.getChildren().get(0);
				params += " " + ((LeafNode)paramNode.getChildren().get(0)).getToken().getTokenString();
				neparamsNode = neparamsNode.getChildren().size() < 3 ? null : (RuleNode)neparamsNode.getChildren().get(2);
			} while(neparamsNode != null);
		}
		
		ir.add(".FUNC " + funcName + params);
		
		HashMap<String, TigerType> names = new HashMap<>();
		names.putAll(symbolTable.getVariables().keySet().stream().collect(Collectors.toMap(s -> s, s -> symbolTable.getVariables().get(s).getKey())));
		names.putAll(symbolTable.getFunctions().get(funcName).getValue().stream().collect(Collectors.toMap(Pair::getKey, Pair::getValue)));
		generateStatements((RuleNode)ruleNode.getChildren().get(7), names, null, ir);
		
		ir.add("RET");
	}
	
	private void generateMain(List<String> ir) {
		ir.add(".FUNC main");
		HashMap<String, TigerType> names = new HashMap<>();
		names.putAll(symbolTable.getVariables().keySet().stream().collect(Collectors.toMap(s -> s, s -> symbolTable.getVariables().get(s).getKey())));
		generateStatements((RuleNode)ast.getChildren().get(3), names, null, ir);
		ir.add("RET");
	}
	
	private void generateStatements(RuleNode stmts, HashMap<String, TigerType> names, String breakLabel, List<String> ir) {
		ensureValue(stmts.getRule(), TigerProductionRule.STMTS);
		
		RuleNode stmt = (RuleNode)((RuleNode)stmts.getChildren().get(0)).getChildren().get(0);
		
		Node firstNode = stmt.getChildren().get(0);
		if(firstNode instanceof LeafNode) {
			LeafNode firstLeaf = (LeafNode)firstNode;
			
			switch(firstLeaf.getToken().getTokenClass()) {
				case IF: {
					Pair<String, ParamType> condReg = generateBoolexpr((RuleNode)stmt.getChildren().get(1), names, ir);
					String elseLabel = nextLabel();
					
					ir.add("BRZ " + condReg.getKey() + " " + elseLabel);
					generateStatements((RuleNode)stmt.getChildren().get(3), names, breakLabel, ir);
					
					String endifLabel = null;
					
					if(stmt.getChildren().size() >= 6) {
						endifLabel = nextLabel();
						ir.add("BR " + endifLabel);
					}
					
					ir.add(elseLabel + ":");
					
					if(stmt.getChildren().size() >= 6) {
						generateStatements((RuleNode)stmt.getChildren().get(5), names, breakLabel, ir);
						ir.add(endifLabel + ":");
					}
					
					break;
				}
				case WHILE: {
					String loopLabel = nextLabel();
					String endLabel = nextLabel();
					
					ir.add(loopLabel + ":");
					
					Pair<String, ParamType> condReg = generateBoolexpr((RuleNode)stmt.getChildren().get(1), names, ir);
					ir.add("BRZ " + condReg + " " + endLabel);
					
					generateStatements((RuleNode)stmt.getChildren().get(3), names, endLabel, ir);
					
					ir.add("BR " + loopLabel);
					ir.add(endLabel + ":");
					
					break;
				}
				case FOR: {
					String id = ((LeafNode)stmt.getChildren().get(1)).getToken().getTokenString();
					
					Pair<String, ParamType> idValue = generateNumexpr((RuleNode)stmt.getChildren().get(3), names, true, ir);
					ir.add("ST" + getOpSuffix(idValue.getValue()) + " " + idValue.getKey() + " " + id);
					
					String loopLabel = nextLabel();
					String endLabel = nextLabel();
					
					ir.add(loopLabel + ":");
					
					Pair<String, ParamType> limitValue = generateNumexpr((RuleNode)stmt.getChildren().get(5), names, true, ir);
					
					String condReg = nextIntReg();
					String idReg = nextIntReg();
					ir.add("LDi " + idReg + " " + id);
					ir.add("NEQ" + getOpSuffix(limitValue.getValue()) + " " + condReg + " " + idReg + " " + limitValue.getKey());
					ir.add("BRZ " + condReg + " " + endLabel);
					
					generateStatements((RuleNode)stmt.getChildren().get(7), names, endLabel, ir);
					
					ir.add("LDi " + idReg + " " + id);
					ir.add("ADDIi " + idReg + " " + idReg + " 1");
					ir.add("STi " + idReg + " " + id);
					ir.add("BR " + loopLabel);
					ir.add(endLabel + ":");
					
					break;
				}
				case BREAK: {
					if(breakLabel == null) {
						throw new IllegalArgumentException("Something went very wrong: Break label is null!");
					}
					ir.add("BR " + breakLabel);
					break;
				}
				case RETURN: {
					Pair<String, ParamType> returnValue = generateNumexpr((RuleNode)stmt.getChildren().get(1), names, false, ir);
					ir.add("RET" + getOpSuffix(returnValue.getValue()) + " " + returnValue.getKey());
					break;
				}
				default:
					throw new IllegalStateException("Invalid token class for stmt: " + firstLeaf.getToken().getTokenClass());
			}
		} else {
			RuleNode firstRule = (RuleNode)firstNode;
			
			switch(firstRule.getRule()) {
				case LVALUE: {
					Pair<String, ParamType> reg = generateNumexpr((RuleNode)stmt.getChildren().get(2), names, true, ir);
					handleStoreLvalue(firstRule, reg, names, ir);
					break;
				}
				case OPTSTORE: {
					String funcArgs = "";
					RuleNode numexprs = (RuleNode)stmt.getChildren().get(3);
					if(numexprs.getChildren().size() > 0) {
						RuleNode neexprs = (RuleNode)numexprs.getChildren().get(0);
						while(true) {
							Pair<String, ParamType> argReg = generateNumexpr((RuleNode)neexprs.getChildren().get(0), names, false, ir);
							funcArgs += " " + argReg.getKey();
							if(neexprs.getChildren().size() > 1) {
								neexprs = (RuleNode)neexprs.getChildren().get(2);
							} else {
								break;
							}
						}
					}
					
					String funcName = ((LeafNode)stmt.getChildren().get(1)).getToken().getTokenString();
					
					if(firstRule.getChildren().size() == 0) {
						ir.add("CALL " + funcName + funcArgs);
					} else {
						TigerType.BaseType baseType = symbolTable.getFunctions().get(funcName).getKey().baseType;
						Pair<String, ParamType> returnReg;
						if(baseType == BaseType.INT || baseType == BaseType.ARRAY) {
							returnReg = new Pair<>(nextIntReg(), ParamType.REGISTERi);
						} else if(baseType == BaseType.FLOAT) {
							returnReg = new Pair<>(nextFloatReg(), ParamType.REGISTERf);
						} else {
							throw new IllegalArgumentException("Unknown basetype " + baseType);
						}
						
						ir.add("CALL_RET " + funcName + " " + returnReg.getKey() + funcArgs);
						
						handleStoreLvalue((RuleNode)firstRule.getChildren().get(0), returnReg, names, ir);
					}
					break;
				}
				default:
					throw new IllegalStateException("Invalid production rule for stmt: " + firstRule.getRule());
			}
		}
		
		if(stmts.getChildren().size() == 2) {
			generateStatements((RuleNode)stmts.getChildren().get(1), names, breakLabel, ir);
		}
	}
	
	private void handleStoreLvalue(RuleNode lvalue, Pair<String, ParamType> sourceReg, HashMap<String, TigerType> names, List<String> ir) {
		String name = ((LeafNode)lvalue.getChildren().get(0)).getToken().getTokenString();
		
		RuleNode optoffset = (RuleNode)lvalue.getChildren().get(1);
		if(optoffset.getChildren().size() > 1) { // array access
			TigerArrayType arrayType = (TigerArrayType)names.get(name);
			
			if(arrayType.subType.baseType == BaseType.FLOAT) {
				handleIntToFloat(new Pair<>(null, ParamType.REGISTERf), sourceReg, ir);
			}
			
			Pair<String, ParamType> offset = generateNumexpr((RuleNode)optoffset.getChildren().get(1), names, true, ir);
			
			String addrReg = nextIntReg();
			ir.add("LDi " + addrReg + " " + name);
			String offsetAdd = "ADD" + (offset.getValue() == ParamType.IMMEDIATEi ? "Ii" : "i");
			ir.add(offsetAdd + " " + addrReg + " " + addrReg + " " + offset.getKey());
			
			ir.add("STR" + getOpSuffix(sourceReg.getValue()) + " " + sourceReg.getKey() + " " + addrReg);
		} else {
			if(names.get(name).baseType == BaseType.FLOAT) {
				handleIntToFloat(new Pair<>(null, ParamType.REGISTERf), sourceReg, ir);
			}
			
			ir.add("ST" + getOpSuffix(sourceReg.getValue()) + " " + sourceReg.getKey() + " " + name);
		}
	}
	
	private Pair<String, ParamType> generateNumexpr(RuleNode numexpr, HashMap<String, TigerType> names, boolean returnImm, List<String> ir) {
		ensureValue(numexpr.getRule(), TigerProductionRule.NUMEXPR);
		
		RuleNode firstRule = (RuleNode)numexpr.getChildren().get(0);
		if(firstRule.getRule() == TigerProductionRule.TERM) {
			return generateTerm(firstRule, names, returnImm, ir);
		} else {
			Pair<String, ParamType> numexprChild = generateNumexpr(firstRule, names, false, ir);
			Pair<String, ParamType> termChild = generateTerm((RuleNode)numexpr.getChildren().get(2), names, true, ir);
			
			handleIntToFloat(numexprChild, termChild, ir);
			
			String reg;
			boolean isInt = numexprChild.getValue() == ParamType.REGISTERi;
			if(isInt) {
				reg = nextIntReg();
			} else {
				reg = nextFloatReg();
			}
			
			String op = ((LeafNode)((RuleNode)numexpr.getChildren().get(1)).getChildren().get(0)).getToken().getTokenClass() == TigerTokenClass.PLUS ? "ADD" : "SUB";
			op += getOpSuffix(termChild.getValue());
			ir.add(op + " " + reg + " " + numexprChild.getKey() + " " + termChild.getKey());
			return new Pair<>(reg, isInt ? ParamType.REGISTERi : ParamType.REGISTERf);
		}
	}
	
	private Pair<String, ParamType> generateTerm(RuleNode term, HashMap<String, TigerType> names, boolean returnImm, List<String> ir) {
		ensureValue(term.getRule(), TigerProductionRule.TERM);
		
		RuleNode firstRule = (RuleNode)term.getChildren().get(0);
		if(firstRule.getRule() == TigerProductionRule.FACTOR) {
			return generateFactor(firstRule, names, returnImm, ir);
		} else {
			Pair<String, ParamType> termChild = generateTerm(firstRule, names, false, ir);
			Pair<String, ParamType> factorChild = generateFactor((RuleNode)term.getChildren().get(2), names, true, ir);
			
			handleIntToFloat(termChild, factorChild, ir);
			
			String reg;
			boolean isInt = termChild.getValue() == ParamType.REGISTERi;
			if(isInt) {
				reg = nextIntReg();
			} else {
				reg = nextFloatReg();
			}
			
			String op = ((LeafNode)((RuleNode)term.getChildren().get(1)).getChildren().get(0)).getToken().getTokenClass() == TigerTokenClass.STAR ? "MUL" : "DIV";
			op += getOpSuffix(factorChild.getValue());
			ir.add(op + " " + reg + " " + termChild.getKey() + " " + factorChild.getKey());
			return new Pair<>(reg, isInt ? ParamType.REGISTERi : ParamType.REGISTERf);
		}
	}
	
	private Pair<String, ParamType> generateFactor(RuleNode factor, HashMap<String, TigerType> names, boolean returnImm, List<String> ir) {
		ensureValue(factor.getRule(), TigerProductionRule.FACTOR);
		
		Node firstNode = factor.getChildren().get(0);
		if(firstNode instanceof RuleNode) {
			LeafNode litNode = (LeafNode)((RuleNode)firstNode).getChildren().get(0);
			switch(litNode.getToken().getTokenClass()) {
				case INTLIT:
					if(returnImm) {
						return new Pair<>(litNode.getToken().getTokenString(), ParamType.IMMEDIATEi);
					} else {
						String reg = nextIntReg();
						ir.add("LDIi " + reg + " " + litNode.getToken().getTokenString());
						return new Pair<>(reg, ParamType.REGISTERi);
					}
				case FLOATLIT:
					if(returnImm) {
						return new Pair<>(litNode.getToken().getTokenString(), ParamType.IMMEDIATEf);
					} else {
						String reg = nextFloatReg();
						ir.add("LDIf " + reg + " " + litNode.getToken().getTokenString());
						return new Pair<>(reg, ParamType.REGISTERf);
					}
				default:
					throw new IllegalArgumentException("Invalid const leaf " + litNode.getToken().getTokenClass());
			}
		} else {
			LeafNode firstLeaf = (LeafNode)firstNode;
			
			switch(firstLeaf.getToken().getTokenClass()) {
				case ID: {
					String name = firstLeaf.getToken().getTokenString();
					TigerType type = names.get(name);
					
					if(factor.getChildren().size() > 1) { // array access
						Pair<String, ParamType> offset = generateNumexpr((RuleNode)factor.getChildren().get(2), names, true, ir);
						
						String addrReg = nextIntReg();
						ir.add("LDi " + addrReg + " " + name);
						String offsetAdd = "ADD" + (offset.getValue() == ParamType.IMMEDIATEi ? "Ii" : "i");
						ir.add(offsetAdd + " " + addrReg + " " + addrReg + " " + offset.getKey());
						
						TigerArrayType arrayType = (TigerArrayType)type;
						if(arrayType.subType.baseType == BaseType.INT || arrayType.subType.baseType == BaseType.ARRAY) {
							String reg = nextIntReg();
							ir.add("LDRi " + reg + " " + addrReg);
							return new Pair<>(reg, ParamType.REGISTERi);
						} else if(arrayType.subType.baseType == BaseType.FLOAT) {
							String reg = nextFloatReg();
							ir.add("LDRf " + reg + " " + addrReg);
							return new Pair<>(reg, ParamType.REGISTERf);
						} else {
							throw new IllegalArgumentException("Unrecognized basetype");
						}
					} else {
						if(type.baseType == BaseType.INT || type.baseType == BaseType.ARRAY) {
							String reg = nextIntReg();
							ir.add("LDi " + reg + " " + name);
							return new Pair<>(reg, ParamType.REGISTERi);
						} else if(type.baseType == BaseType.FLOAT) {
							String reg = nextFloatReg();
							ir.add("LDf " + reg + " " + name);
							return new Pair<>(reg, ParamType.REGISTERf);
						} else {
							throw new IllegalArgumentException("Unrecognized basetype");
						}
					}
				}
				case LPAREN:
					return generateNumexpr((RuleNode)factor.getChildren().get(1), names, returnImm, ir);
				default:
					throw new IllegalArgumentException("Invalid token class for factor: " + firstLeaf.getToken().getTokenClass());
			}
		}
	}
	
	private Pair<String, ParamType> generateBoolexpr(RuleNode boolexpr, HashMap<String, TigerType> names, List<String> ir) {
		ensureValue(boolexpr.getRule(), TigerProductionRule.BOOLEXPR);
		
		RuleNode firstRule = (RuleNode)boolexpr.getChildren().get(0);
		if(firstRule.getRule() == TigerProductionRule.CLAUSE) {
			return generateClause(firstRule, names, ir);
		} else {
			Pair<String, ParamType> boolexprChild = generateBoolexpr(firstRule, names, ir);
			Pair<String, ParamType> clauseChild = generateClause((RuleNode)boolexpr.getChildren().get(2), names, ir);
			
			handleIntToFloat(boolexprChild, clauseChild, ir);
			
			String reg = nextIntReg();
			ir.add("OR" + getOpSuffix(clauseChild.getValue()) + " " + reg + " " + boolexprChild.getKey() + " " + clauseChild.getKey());
			return new Pair<>(reg, ParamType.REGISTERi);
		}
	}
	
	private Pair<String, ParamType> generateClause(RuleNode clause, HashMap<String, TigerType> names, List<String> ir) {
		ensureValue(clause.getRule(), TigerProductionRule.CLAUSE);
		
		RuleNode firstRule = (RuleNode)clause.getChildren().get(0);
		if(firstRule.getRule() == TigerProductionRule.PRED) {
			return generatePred(firstRule, names, ir);
		} else {
			Pair<String, ParamType> clauseChild = generateClause(firstRule, names, ir);
			Pair<String, ParamType> predChild = generatePred((RuleNode)clause.getChildren().get(2), names, ir);
			
			handleIntToFloat(clauseChild, predChild, ir);
			
			String reg = nextIntReg();
			ir.add("AND" + getOpSuffix(predChild.getValue()) + " " + reg + " " + clauseChild.getKey() + " " + predChild.getKey());
			return new Pair<>(reg, ParamType.REGISTERi);
		}
	}
	
	private Pair<String, ParamType> generatePred(RuleNode pred, HashMap<String, TigerType> names, List<String> ir) {
		ensureValue(pred.getRule(), TigerProductionRule.PRED);
		
		Node firstNode = pred.getChildren().get(0);
		if(firstNode instanceof LeafNode) {
			return generateBoolexpr((RuleNode)pred.getChildren().get(1), names, ir);
		} else {
			Pair<String, ParamType> leftNumexpr, rightNumexpr;
			
			LeafNode boolop = (LeafNode)((RuleNode)pred.getChildren().get(1)).getChildren().get(0);
			if(boolop.getToken().getTokenClass() == TigerTokenClass.LT || boolop.getToken().getTokenClass() == TigerTokenClass.LEQUAL) {
				leftNumexpr = generateNumexpr((RuleNode)pred.getChildren().get(2), names, false, ir);
				rightNumexpr = generateNumexpr((RuleNode)firstNode, names, true, ir);
				boolop = new LeafNode(new TigerToken(boolop.getToken().getTokenClass() == TigerTokenClass.LT ? TigerTokenClass.GT : TigerTokenClass.GEQUAL, "", "", 0, 0));
			} else {
				leftNumexpr = generateNumexpr((RuleNode)firstNode, names, false, ir);
				rightNumexpr = generateNumexpr((RuleNode)pred.getChildren().get(2), names, true, ir);
			}
			
			handleIntToFloat(leftNumexpr, rightNumexpr, ir);
			
			String reg = nextIntReg();
			ir.add(getBoolop(boolop) + getOpSuffix(rightNumexpr.getValue()) + " " + reg + " " + leftNumexpr.getKey() + " " + rightNumexpr.getKey());
			return new Pair<>(reg, ParamType.REGISTERi);
		}
	}
	
	private int intRegCount = 0;
	
	private String nextIntReg() {
		return "$i" + intRegCount++;
	}
	
	private int floatRegCount = 0;
	
	private String nextFloatReg() {
		return "$f" + floatRegCount++;
	}
	
	private int labelCount = 0;
	
	private String nextLabel() {
		return "Label" + labelCount++;
	}
	
	private TigerType.BaseType getParamType(TigerType type) {
		switch(type.baseType) {
			case INT:
				return BaseType.INT;
			case FLOAT:
				return BaseType.FLOAT;
			case ARRAY:
				return getParamType(((TigerArrayType)type).subType);
		}
		
		return null;
	}
	
	private void handleIntToFloat(Pair<String, ParamType> leftParam, Pair<String, ParamType> rightParam, List<String> ir) {
		if(leftParam.getValue() == ParamType.REGISTERi && rightParam.getValue() == ParamType.REGISTERf) {
			String tmpReg = nextFloatReg();
			ir.add("ITOF " + tmpReg + " " + leftParam.getKey());
			leftParam.set(tmpReg, ParamType.REGISTERf);
		} else if(leftParam.getValue() == ParamType.REGISTERf) {
			if(rightParam.getValue() == ParamType.REGISTERi) {
				String tmpReg = nextFloatReg();
				ir.add("ITOF " + tmpReg + " " + rightParam.getKey());
				rightParam.set(tmpReg, ParamType.REGISTERf);
			} else if(rightParam.getValue() == ParamType.IMMEDIATEi) {
				String tmpReg = nextIntReg();
				String tmpRegf = nextFloatReg();
				ir.add("LDIi " + tmpReg + " " + rightParam.getKey());
				ir.add("ITOF " + tmpRegf + " " + tmpReg);
				rightParam.set(tmpRegf, ParamType.REGISTERf);
			}
		}
	}
	
	private String getBoolop(LeafNode leafNode) {
		switch(leafNode.getToken().getTokenClass()) {
			case EQUAL:
				return "EQ";
			case NOTEQUAL:
				return "NEQ";
			case GT:
				return "GT";
			case GEQUAL:
				return "LT";
		}
		
		throw new IllegalArgumentException("Invalid boolop " + leafNode.getToken());
	}
	
	private String getOpSuffix(ParamType type) {
		switch(type) {
			case IMMEDIATEi:
				return "Ii";
			case IMMEDIATEf:
				return "If";
			case REGISTERi:
				return "i";
			case REGISTERf:
				return "f";
			default:
				throw new IllegalArgumentException("Invalid op type");
		}
	}
}
