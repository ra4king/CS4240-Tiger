package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.ArrayList;
import java.util.Collections;
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
public class TigerSymbolTable {
	private RuleNode ast;
	private HashMap<String, RuleNode> typeAliases;
	private HashMap<String, RuleNode> variables;
	private HashMap<String, Pair<RuleNode, List<Pair<String, RuleNode>>>> functions;
	
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
	
	public HashMap<String, RuleNode> getVariables() {
		return variables;
	}
	
	public HashMap<String, Pair<RuleNode, List<Pair<String, RuleNode>>>> getFunctions() {
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
		RuleNode type = (RuleNode)typedecl.getChildren().get(3);
		getBaseType(type);
		
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
		RuleNode type = getBaseType((RuleNode)vardecl.getChildren().get(3));
		
		buildIds(ids, type);
		
		buildVardecls((RuleNode)vardecls.getChildren().get(1));
	}
	
	private void buildIds(RuleNode ids, RuleNode type) {
		ensureValue(ids.getValue(), TigerProductionRule.IDS);
		
		for(Node child : ids.getChildren()) {
			if(child instanceof RuleNode) {
				buildIds((RuleNode)child, type);
			} else {
				TigerToken token = ((LeafNode)child).getToken();
				if(token.getTokenClass() == TigerTokenClass.ID) {
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
		
		List<Pair<String, RuleNode>> argumentTypes;
		
		if(params.getChildren().size() == 1) {
			argumentTypes = buildFuncArgs((RuleNode)params.getChildren().get(0));
		} else {
			argumentTypes = new ArrayList<>();
		}
		
		if(optrettype.getChildren().size() != 0) {
			RuleNode type = getBaseType((RuleNode)optrettype.getChildren().get(1));
			functions.put(id.getToken(), new Pair<>(type, argumentTypes));
		} else {
			functions.put(id.getToken(), new Pair<>(null, argumentTypes)); // null == void
		}
		
		buildFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private List<Pair<String, RuleNode>> buildFuncArgs(RuleNode params) throws TigerParseException {
		ensureValue(params.getValue(), TigerProductionRule.NEPARAMS);
		
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
						
						RuleNode type = getBaseType((RuleNode)ruleNode.getChildren().get(2));
						
						argumentTypes.add(new Pair<>(id.getToken(), type));
						break;
					default:
						throw new IllegalArgumentException("Expecting NEPARAMS or PARAM, received " + ruleNode.getValue());
				}
			}
		}
		
		return argumentTypes;
	}
	
	public void addSpecialFunctions() {
		final RuleNode INT_TYPE = new RuleNode(TigerProductionRule.TYPE, new LeafNode(new TigerToken(TigerTokenClass.INT, "int", "", 0, 0)));
		final RuleNode FLOAT_TYPE = new RuleNode(TigerProductionRule.TYPE, new LeafNode(new TigerToken(TigerTokenClass.FLOAT, "float", "", 0, 0)));
		final RuleNode BOOL_TYPE = new RuleNode(TigerProductionRule.TYPE, new LeafNode(new TigerToken(TigerTokenClass.BOOL, "bool", "", 0, 0)));
		
		functions.put("printi", new Pair<>(null, Collections.singletonList(new Pair<>("i", INT_TYPE))));
		functions.put("printf", new Pair<>(null, Collections.singletonList(new Pair<>("f", FLOAT_TYPE))));
		functions.put("printb", new Pair<>(null, Collections.singletonList(new Pair<>("b", BOOL_TYPE))));
	}
	
	private RuleNode getBaseType(RuleNode type) throws TigerParseException {
		ensureValue(type.getValue(), TigerProductionRule.TYPE);
		
		switch(((LeafNode)type.getChildren().get(0)).getToken().getTokenClass()) {
			case ID:
				return followAliases(type);
			case ARRAY:
				RuleNode fixed = new RuleNode(type);
				fixed.getChildren().add(getBaseType((RuleNode)fixed.getChildren().remove(5)));
				fixed.getChildren().add(2, new LeafNode(getLiteralType(((LeafNode)fixed.getChildren().remove(2)).getToken())));
				return fixed;
			default:
				return type;
		}
	}
	
	private RuleNode followAliases(RuleNode type) throws TigerParseException {
		ensureValue(type.getValue(), TigerProductionRule.TYPE);
		
		while(((LeafNode)type.getChildren().get(0)).getToken().getTokenClass() == TigerTokenClass.ID) {
			RuleNode tmp = typeAliases.get(((LeafNode)type.getChildren().get(0)).getToken().getToken());
			if(tmp == null) {
				throw new TigerParseException("Unknown type", ((LeafNode)type.getChildren().get(0)).getToken());
			}
			type = tmp;
		}
		
		return type;
	}
}
