package edu.cs4240.tiger.analyzer;

import java.util.ArrayList;
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
	private HashMap<String, RuleNode> typeAliases;
	private HashMap<String, RuleNode> varTypes;
	private HashMap<String, Pair<RuleNode, List<Pair<String, RuleNode>>>> funcTypes;
	
	public TigerAnalyzer(RuleNode ast) throws TigerParseException {
		this.ast = ast;
		
		buildSymbolTable();
		analyzeFunction();
		
		System.out.println("Types:");
		for(String s : typeAliases.keySet()) {
			System.out.println(s + " - " + typeAliases.get(s).toString());
		}
		
		System.out.println("\nVars:");
		for(String s : varTypes.keySet()) {
			System.out.println(s + " - " + varTypes.get(s).toString());
		}
		
		System.out.println("\nFuncs:");
		for(String s : funcTypes.keySet()) {
			System.out.println(s + " - " + funcTypes.get(s).toString());
		}
	}
	
	private void analyzeStatement(RuleNode stmt, HashMap<String, RuleNode> varTypes) {
		
	}
	
	private RuleNode analyzeNumexprType(RuleNode numexpr, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		if(numexpr.getValue() != TigerProductionRule.NUMEXPR) {
			throw new IllegalArgumentException("Expected NUMEXPR, received " + numexpr.getValue());
		}
		
		RuleNode child = (RuleNode)numexpr.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.NUMEXPR) {
			RuleNode leftTypeRule = analyzeNumexprType(child, varTypes);
			RuleNode rightTypeRule = analyzeTermType((RuleNode)numexpr.getChildren().get(2), varTypes);
			
			LeafNode leftType = (LeafNode)leftTypeRule.getChildren().get(0);
			LeafNode rightType = (LeafNode)rightTypeRule.getChildren().get(0);
			
			if(leftType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     leftType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on INT and FLOAT types.", leftType.getToken());
			}
			
			if(rightType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     rightType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on INT and FLOAT types.", rightType.getToken());
			}
			
			if(leftType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return leftTypeRule;
			}
			
			if(rightType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return rightTypeRule;
			}
			
			return leftTypeRule;
		} else {
			return analyzeTermType(child, varTypes);
		}
	}
	
	private RuleNode analyzeTermType(RuleNode term, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		if(term.getValue() != TigerProductionRule.TERM) {
			throw new IllegalArgumentException("Expected TERM, received " + term.getValue());
		}
		
		RuleNode child = (RuleNode)term.getChildren().get(0);
		if(child.getValue() == TigerProductionRule.TERM) {
			RuleNode leftTypeRule = analyzeTermType(child, varTypes);
			RuleNode rightTypeRule = analyzeFactorType((RuleNode)term.getChildren().get(2), varTypes);
			
			LeafNode leftType = (LeafNode)leftTypeRule.getChildren().get(0);
			LeafNode rightType = (LeafNode)rightTypeRule.getChildren().get(0);
			
			if(leftType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     leftType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on INT and FLOAT.", leftType.getToken());
			}
			
			if(rightType.getToken().getTokenClass() != TigerTokenClass.INT &&
			     rightType.getToken().getTokenClass() != TigerTokenClass.FLOAT) {
				throw new TigerParseException("Operator can only be applied on INT and FLOAT.", rightType.getToken());
			}
			
			if(leftType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return leftTypeRule;
			}
			
			if(rightType.getToken().getTokenClass() == TigerTokenClass.FLOAT) {
				return rightTypeRule;
			}
			
			return leftTypeRule;
		} else {
			return analyzeFactorType(child, varTypes);
		}
	}
	
	private RuleNode analyzeFactorType(RuleNode factor, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		if(factor.getValue() != TigerProductionRule.FACTOR) {
			throw new IllegalArgumentException("Expected FACTOR, received " + factor.getValue());
		}
		
		Node first = factor.getChildren().get(0);
		if(first instanceof LeafNode) {
			LeafNode firstLeaf = (LeafNode)first;
			
			switch(firstLeaf.getToken().getTokenClass()) {
				case ID:
					RuleNode idType = getBaseTypeOfId(firstLeaf.getToken(), varTypes);
					
					if(factor.getChildren().size() == 1) {
						return idType;
					} else {
						RuleNode indexType = analyzeNumexprType((RuleNode)factor.getChildren().get(2), varTypes);
						if(((LeafNode)indexType.getChildren().get(0)).getToken().getTokenClass() != TigerTokenClass.INT) {
							throw new TigerParseException("Array index must be an integer type", ((LeafNode)factor.getChildren().get(1)).getToken());
						}
						
						if(((LeafNode)idType.getChildren().get(0)).getToken().getTokenClass() != TigerTokenClass.ARRAY) {
							LeafNode leftmostLeaf = getLeftmostLeaf((RuleNode)factor.getChildren().get(2));
							throw new TigerParseException("Non-array types cannot be indexed into", leftmostLeaf == null ? null : leftmostLeaf.getToken());
						}
						
						return (RuleNode)idType.getChildren().get(5);
					}
				case LPAREN:
					return analyzeNumexprType((RuleNode)factor.getChildren().get(1), varTypes);
				default:
					throw new TigerParseException("Something went very wrong", firstLeaf.getToken());
			}
		} else {
			RuleNode firstRule = (RuleNode)first;
			
			switch(firstRule.getValue()) {
				case CONST:
					return new RuleNode(TigerProductionRule.TYPE, new LeafNode(((LeafNode)firstRule.getChildren().get(0)).getToken()));
				default:
					LeafNode leftmostLeaf = getLeftmostLeaf(firstRule);
					throw new TigerParseException("Something went very wrong", leftmostLeaf == null ? null : leftmostLeaf.getToken());
			}
		}
	}
	
	private RuleNode getBaseTypeOfId(TigerToken token, HashMap<String, RuleNode> varTypes) throws TigerParseException {
		RuleNode type = varTypes.get(token.getToken());
		if(type == null) {
			throw new TigerParseException("Undeclared variable.", token);
		}
		return getBaseType(type);
	}
	
	private RuleNode getBaseType(RuleNode type) throws TigerParseException {
		if(type.getValue() != TigerProductionRule.TYPE) {
			throw new IllegalArgumentException("Expected TYPE, received " + type.getValue());
		}
		
		while(((LeafNode)type.getChildren().get(0)).getToken().getTokenClass() == TigerTokenClass.ID) {
			RuleNode tmp = typeAliases.get(((LeafNode)type.getChildren().get(0)).getToken().getToken());
			if(tmp == null) {
				throw new TigerParseException("Unknown type", ((LeafNode)type.getChildren().get(0)).getToken());
			}
			type = tmp;
		}
		
		return type;
	}
	
	private void analyzeFunction() throws TigerParseException {
		analyzeFunctions((RuleNode)((RuleNode)ast.getChildren().get(1)).getChildren().get(2));
	}
	
	private void analyzeFunctions(RuleNode funcdecls) throws TigerParseException {
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		analyzeFunctionStmts(funcTypes.get(((LeafNode)funcdecl.getChildren().get(1)).getToken().getToken()), (RuleNode)funcdecl.getChildren().get(7));
		analyzeFunctions((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private void analyzeFunctionStmts(Pair<RuleNode, List<Pair<String, RuleNode>>> funcInfo, RuleNode stmts) throws TigerParseException {
		if(funcInfo == null) {
			throw new IllegalStateException("funcInfo is null somehow...");
		}
		
		HashMap<String, RuleNode> funcVarTypes = new HashMap<>();
		funcVarTypes.putAll(varTypes);
		funcInfo.getValue().forEach((Pair<String, RuleNode> p) -> funcVarTypes.put(p.getKey(), p.getValue()));
		
		RuleNode stmt = (RuleNode)((RuleNode)stmts.getChildren().get(0)).getChildren().get(0);
		Node first = stmt.getChildren().get(0);
		
		if(first instanceof LeafNode && ((LeafNode)first).getToken().getTokenClass() == TigerTokenClass.RETURN) {
			RuleNode returnType = analyzeNumexprType((RuleNode)stmt.getChildren().get(1), funcVarTypes);
			if(!returnType.equals(funcInfo.getKey())) {
				throw new TigerParseException("Type of returned expression does not match return type", ((LeafNode)first).getToken());
			}
		} else {
			analyzeStatement(stmt, funcVarTypes);
		}
		
		if(stmts.getChildren().size() > 1) {
			analyzeFunctionStmts(funcInfo, (RuleNode)stmts.getChildren().get(1));
		}
	}
	
	private void buildSymbolTable() throws TigerParseException {
		typeAliases = new HashMap<>();
		varTypes = new HashMap<>();
		funcTypes = new HashMap<>();
		
		List<Node> declseg = ((RuleNode)ast.getChildren().get(1)).getChildren();
		buildTypedecls((RuleNode)declseg.get(0));
		buildVardecls((RuleNode)declseg.get(1));
		buildFuncdecls((RuleNode)declseg.get(2));
	}
	
	private void verifyType(RuleNode type) throws TigerParseException {
		if(type.getValue() != TigerProductionRule.TYPE) {
			throw new IllegalArgumentException("Expected TYPE, received " + type.getValue());
		}
		
		switch(((LeafNode)type.getChildren().get(0)).getToken().getTokenClass()) {
			case ID:
				getBaseType(type);
				break;
			case ARRAY:
				verifyType((RuleNode)type.getChildren().get(5));
				break;
		}
	}
	
	private void buildTypedecls(RuleNode typedecls) throws TigerParseException {
		if(typedecls.getValue() != TigerProductionRule.TYPEDECLS) {
			throw new IllegalArgumentException("Expected TYPEDECLS, received " + typedecls.getValue());
		}
		
		if(typedecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode typedecl = (RuleNode)typedecls.getChildren().get(0);
		TigerToken id = ((LeafNode)typedecl.getChildren().get(1)).getToken();
		RuleNode type = (RuleNode)typedecl.getChildren().get(3);
		verifyType(type);
		
		typeAliases.put(id.getToken(), type);
		
		buildTypedecls((RuleNode)typedecls.getChildren().get(1));
	}
	
	private void buildVardecls(RuleNode vardecls) throws TigerParseException {
		if(vardecls.getValue() != TigerProductionRule.VARDECLS) {
			throw new IllegalArgumentException("Expected VARDECLS, received " + vardecls.getValue());
		}
		
		if(vardecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode vardecl = (RuleNode)vardecls.getChildren().get(0);
		RuleNode ids = (RuleNode)vardecl.getChildren().get(1);
		RuleNode type = (RuleNode)vardecl.getChildren().get(3);
		verifyType(type);
		
		buildIds(ids, type);
		
		buildVardecls((RuleNode)vardecls.getChildren().get(1));
	}
	
	private void buildIds(RuleNode ids, RuleNode type) {
		if(ids.getValue() != TigerProductionRule.IDS) {
			throw new IllegalArgumentException("Expected IDS, received " + ids.getValue());
		}
		
		for(Node child : ids.getChildren()) {
			if(child instanceof RuleNode) {
				buildIds((RuleNode)child, type);
			} else {
				TigerToken token = ((LeafNode)child).getToken();
				if(token.getTokenClass() == TigerTokenClass.ID) {
					varTypes.put(token.getToken(), type);
				}
			}
		}
	}
	
	private void buildFuncdecls(RuleNode funcdecls) throws TigerParseException {
		if(funcdecls.getValue() != TigerProductionRule.FUNCDECLS) {
			throw new IllegalArgumentException("Expected FUNDECLS, received " + funcdecls.getValue());
		}
		
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		TigerToken id = ((LeafNode)funcdecl.getChildren().get(1)).getToken();
		RuleNode params = (RuleNode)funcdecl.getChildren().get(3);
		RuleNode optrettype = (RuleNode)funcdecl.getChildren().get(5);
		
		List<Pair<String, RuleNode>> argumentTypes;
		
		if(params.getChildren().size() == 1) {
			argumentTypes = buildFuncArgs((RuleNode)params.getChildren().get(0));
		} else {
			argumentTypes = new ArrayList<>();
		}
		
		if(optrettype.getChildren().size() != 0) {
			RuleNode type = (RuleNode)optrettype.getChildren().get(1);
			verifyType(type);
			funcTypes.put(id.getToken(), new Pair<>(type, argumentTypes));
		} else {
			funcTypes.put(id.getToken(), new Pair<>(null, argumentTypes)); // null == void
		}
		
		buildFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private List<Pair<String, RuleNode>> buildFuncArgs(RuleNode params) throws TigerParseException {
		if(params.getValue() != TigerProductionRule.NEPARAMS) {
			throw new IllegalArgumentException("Expected NEPARAMS, received " + params.getValue());
		}
		
		ArrayList<Pair<String, RuleNode>> argumentTypes = new ArrayList<>();
		
		for(Node child : params.getChildren()) {
			if(child instanceof RuleNode) {
				RuleNode ruleNode = (RuleNode)child;
				
				switch(ruleNode.getValue()) {
					case NEPARAMS:
						argumentTypes.addAll(buildFuncArgs((RuleNode)child));
						break;
					case PARAM:
						TigerToken id = ((LeafNode)ruleNode.getChildren().get(0)).getToken();
						if(id.getTokenClass() != TigerTokenClass.ID) {
							throw new IllegalArgumentException("Expecting ID, received " + id.getTokenClass());
						}
						
						RuleNode type = (RuleNode)ruleNode.getChildren().get(2);
						verifyType(type);
						
						argumentTypes.add(new Pair<>(id.getToken(), type));
						break;
					default:
						throw new IllegalArgumentException("Expecting NEPARAMS or PARAM, received " + ruleNode.getValue());
				}
			}
		}
		
		return argumentTypes;
	}
	
	private static LeafNode getLeftmostLeaf(RuleNode node) {
		if(node.getChildren().size() == 0)
			return null;
		
		Node first = node.getChildren().get(0);
		if(first instanceof LeafNode)
			return (LeafNode)first;
		
		return getLeftmostLeaf((RuleNode)first);
	}
}
