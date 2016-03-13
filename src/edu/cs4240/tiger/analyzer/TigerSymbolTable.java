package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cs4240.tiger.analyzer.TigerType.TigerArrayType;
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
public class TigerSymbolTable {
	private RuleNode ast;
	private HashMap<String, TigerType> typeAliases;
	private HashMap<String, TigerType> variables;
	private HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> functions;
	
	public TigerSymbolTable(RuleNode ast) throws TigerParseException {
		this.ast = ast;
		buildSymbolTable();
	}
	
	public void printSymbolTables() {
		System.out.println("Types:");
		for(String s : typeAliases.keySet()) {
			System.out.println(s + " - " + typeAliases.get(s).toString());
		}
		
		System.out.println("\nVars:");
		for(String s : variables.keySet()) {
			System.out.println(s + " - " + variables.get(s).toString());
		}
		
		System.out.println("\nFuncs:");
		for(String s : functions.keySet()) {
			System.out.println(s + " - " + functions.get(s).toString());
		}
	}
	
	public HashMap<String, TigerType> getVariables() {
		return variables;
	}
	
	public HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> getFunctions() {
		return functions;
	}
	
	private void buildSymbolTable() throws TigerParseException {
		typeAliases = new HashMap<>();
		variables = new HashMap<>();
		functions = new HashMap<>();
		
		List<Node> declseg = ((RuleNode)ast.getChildren().get(1)).getChildren();
		buildTypedecls((RuleNode)declseg.get(0));
		buildVardecls((RuleNode)declseg.get(1));
		buildFuncdecls((RuleNode)declseg.get(2));
		
		addSpecialFunctions();
	}
	
	private void buildTypedecls(RuleNode typedecls) throws TigerParseException {
		ensureValue(typedecls.getValue(), TigerProductionRule.TYPEDECLS);
		
		if(typedecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode typedecl = (RuleNode)typedecls.getChildren().get(0);
		TigerToken id = ((LeafNode)typedecl.getChildren().get(1)).getToken();
		TigerType type = getBaseType((RuleNode)typedecl.getChildren().get(3));
		
		if(typeAliases.get(id.getToken()) != null) {
			throw new TigerParseException("Type previously declared", id);
		}
		
		typeAliases.put(id.getToken(), type);
		
		buildTypedecls((RuleNode)typedecls.getChildren().get(1));
	}
	
	private void buildVardecls(RuleNode vardecls) throws TigerParseException {
		ensureValue(vardecls.getValue(), TigerProductionRule.VARDECLS);
		
		if(vardecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode vardecl = (RuleNode)vardecls.getChildren().get(0);
		RuleNode ids = (RuleNode)vardecl.getChildren().get(1);
		TigerType type = getBaseType((RuleNode)vardecl.getChildren().get(3));
		
		buildIds(ids, type);
		
		RuleNode optinit = (RuleNode)vardecl.getChildren().get(4);
		if(optinit.getChildren().size() > 0) {
			TigerToken constToken = ((LeafNode)((RuleNode)optinit.getChildren().get(1)).getChildren().get(0)).getToken();
			
			if(!TigerTypeAnalyzer.isTypeCompatibleAssign(type, TigerType.getLiteralType(constToken))) {
				throw new TigerParseException("Incompatible types", constToken);
			}
		}
		
		buildVardecls((RuleNode)vardecls.getChildren().get(1));
	}
	
	private void buildIds(RuleNode ids, TigerType type) throws TigerParseException {
		ensureValue(ids.getValue(), TigerProductionRule.IDS);
		
		for(Node child : ids.getChildren()) {
			if(child instanceof RuleNode) {
				buildIds((RuleNode)child, type);
			} else {
				TigerToken token = ((LeafNode)child).getToken();
				if(token.getTokenClass() == TigerTokenClass.ID) {
					if(variables.get(token.getToken()) != null) {
						throw new TigerParseException("Variable previously declared", token);
					}
					
					variables.put(token.getToken(), type);
				}
			}
		}
	}
	
	private void buildFuncdecls(RuleNode funcdecls) throws TigerParseException {
		ensureValue(funcdecls.getValue(), TigerProductionRule.FUNCDECLS);
		
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		TigerToken id = ((LeafNode)funcdecl.getChildren().get(1)).getToken();
		RuleNode params = (RuleNode)funcdecl.getChildren().get(3);
		RuleNode optrettype = (RuleNode)funcdecl.getChildren().get(5);
		
		if(functions.get(id.getToken()) != null) {
			throw new TigerParseException("Function previously declared", id);
		}
		
		ArrayList<Pair<String, TigerType>> argumentTypes = new ArrayList<>();
		
		if(params.getChildren().size() == 1) {
			buildFuncArgs((RuleNode)params.getChildren().get(0), argumentTypes);
		}
		
		if(optrettype.getChildren().size() != 0) {
			TigerType type = getBaseType((RuleNode)optrettype.getChildren().get(1));
			functions.put(id.getToken(), new Pair<>(type, argumentTypes));
		} else {
			functions.put(id.getToken(), new Pair<>(null, argumentTypes)); // null == void
		}
		
		buildFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private void buildFuncArgs(RuleNode params, List<Pair<String, TigerType>> argumentTypes) throws TigerParseException {
		ensureValue(params.getValue(), TigerProductionRule.NEPARAMS);
		
		for(Node child : params.getChildren()) {
			if(child instanceof RuleNode) {
				RuleNode ruleNode = (RuleNode)child;
				
				switch(ruleNode.getValue()) {
					case NEPARAMS:
						buildFuncArgs((RuleNode)child, argumentTypes);
						break;
					case PARAM:
						TigerToken id = ((LeafNode)ruleNode.getChildren().get(0)).getToken();
						ensureValue(id.getTokenClass(), TigerTokenClass.ID);
						
						TigerType type = getBaseType((RuleNode)ruleNode.getChildren().get(2));
						
						for(Pair<String, TigerType> pair : argumentTypes) {
							if(pair.getKey().equals(id.getToken())) {
								throw new TigerParseException("Argument previously declared", id);
							}
						}
						
						argumentTypes.add(new Pair<>(id.getToken(), type));
						break;
					default:
						throw new IllegalArgumentException("Expecting NEPARAMS or PARAM, received " + ruleNode.getValue());
				}
			}
		}
	}
	
	private void addSpecialFunctions() {
		functions.put("printi", new Pair<>(null, Collections.singletonList(new Pair<>("i", TigerType.INT_TYPE))));
		functions.put("printf", new Pair<>(null, Collections.singletonList(new Pair<>("f", TigerType.FLOAT_TYPE))));
		functions.put("printb", new Pair<>(null, Collections.singletonList(new Pair<>("b", TigerType.BOOL_TYPE))));
		functions.put("readi", new Pair<>(TigerType.INT_TYPE, Collections.emptyList()));
		functions.put("readf", new Pair<>(TigerType.FLOAT_TYPE, Collections.emptyList()));
		functions.put("readb", new Pair<>(TigerType.BOOL_TYPE, Collections.emptyList()));
	}
	
	private TigerType getBaseType(RuleNode typeNode) throws TigerParseException {
		ensureValue(typeNode.getValue(), TigerProductionRule.TYPE);
		
		TigerToken token = ((LeafNode)typeNode.getChildren().get(0)).getToken();
		if(token.getTokenClass() == TigerTokenClass.ARRAY) {
			return new TigerArrayType(getBaseType((RuleNode)typeNode.getChildren().get(5)));
		} else if(token.getTokenClass() == TigerTokenClass.ID) {
			TigerType tmp = typeAliases.get(token.getToken());
			if(tmp == null) {
				throw new TigerParseException("Unknown type", token);
			}
			return tmp;
		} else {
			return TigerType.getLiteralType(token);
		}
	}
}
