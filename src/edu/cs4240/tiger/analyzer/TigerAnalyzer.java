package edu.cs4240.tiger.analyzer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
	
	public TigerAnalyzer(RuleNode ast) {
		this.ast = ast;
		
		buildSymbolTable();
		analyzeFunctions();
		
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
	
	private void analyzeFunctions() {
	}
	
	private void buildSymbolTable() {
		typeAliases = new HashMap<>();
		varTypes = new HashMap<>();
		funcTypes = new HashMap<>();
		
		List<Node> declseg = ((RuleNode)ast.getChildren().get(1)).getChildren();
		for(Node node : declseg) {
			RuleNode ruleNode = (RuleNode)node;
			if(ruleNode.getChildren().size() == 0) {
				continue;
			}
			
			switch(ruleNode.getValue()) {
				case TYPEDECLS:
					analyzeTypedecls(ruleNode);
					break;
				case VARDECLS:
					analyzeVardecls(ruleNode);
					break;
				case FUNCDECLS:
					analyzeFuncdecls(ruleNode);
					break;
			}
		}
	}
	
	private void analyzeTypedecls(RuleNode typedecls) {
		if(typedecls.getValue() != TigerProductionRule.TYPEDECLS) {
			throw new IllegalArgumentException("Expected TYPEDECLS, received " + typedecls.getValue());
		}
		
		if(typedecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode typedecl = (RuleNode)typedecls.getChildren().get(0);
		TigerToken id = ((LeafNode)typedecl.getChildren().get(1)).getToken();
		RuleNode type = (RuleNode)typedecl.getChildren().get(3);
		
		typeAliases.put(id.getToken(), type);
		
		analyzeTypedecls((RuleNode)typedecls.getChildren().get(1));
	}
	
	private void analyzeVardecls(RuleNode vardecls) {
		if(vardecls.getValue() != TigerProductionRule.VARDECLS) {
			throw new IllegalArgumentException("Expected VARDECLS, received " + vardecls.getValue());
		}
		
		if(vardecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode vardecl = (RuleNode)vardecls.getChildren().get(0);
		RuleNode ids = (RuleNode)vardecl.getChildren().get(1);
		RuleNode type = (RuleNode)vardecl.getChildren().get(3);
		
		analyzeIds(ids, type);
		
		analyzeVardecls((RuleNode)vardecls.getChildren().get(1));
	}
	
	private void analyzeIds(RuleNode ids, RuleNode type) {
		if(ids.getValue() != TigerProductionRule.IDS) {
			throw new IllegalArgumentException("Expected IDS, received " + ids.getValue());
		}
		
		for(Node child : ids.getChildren()) {
			if(child instanceof RuleNode) {
				analyzeIds((RuleNode)child, type);
			} else {
				TigerToken token = ((LeafNode)child).getToken();
				if(token.getTokenClass() == TigerTokenClass.ID) {
					varTypes.put(token.getToken(), type);
				}
			}
		}
	}
	
	private void analyzeFuncdecls(RuleNode funcdecls) {
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
			argumentTypes = analyzeFuncArgs((RuleNode)params.getChildren().get(0));
		} else {
			argumentTypes = new ArrayList<>();
		}
		
		if(optrettype.getChildren().size() != 0) {
			funcTypes.put(id.getToken(), new Pair<>((RuleNode)optrettype.getChildren().get(1), argumentTypes));
		} else {
			funcTypes.put(id.getToken(), new Pair<>(null, argumentTypes)); // null == void
		}
		
		analyzeFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private List<Pair<String, RuleNode>> analyzeFuncArgs(RuleNode params) {
		if(params.getValue() != TigerProductionRule.NEPARAMS) {
			throw new IllegalArgumentException("Expected NEPARAMS, received " + params.getValue());
		}
		
		ArrayList<Pair<String, RuleNode>> argumentTypes = new ArrayList<>();
		
		for(Node child : params.getChildren()) {
			if(child instanceof RuleNode) {
				RuleNode ruleNode = (RuleNode)child;
				
				switch(ruleNode.getValue()) {
					case NEPARAMS:
						argumentTypes.addAll(analyzeFuncArgs((RuleNode)child));
						break;
					case PARAM:
						TigerToken id = ((LeafNode)ruleNode.getChildren().get(0)).getToken();
						if(id.getTokenClass() != TigerTokenClass.ID) {
							throw new IllegalArgumentException("Expecting ID, received " + id.getTokenClass());
						}
						
						RuleNode type = (RuleNode)ruleNode.getChildren().get(2);
						if(type.getValue() != TigerProductionRule.TYPE) {
							throw new IllegalArgumentException("Expecting TYPE, received " + type.getValue());
						}
						argumentTypes.add(new Pair<>(id.getToken(), type));
						break;
					default:
						throw new IllegalArgumentException("Expecting NEPARAMS or PARAM, received " + ruleNode.getValue());
				}
			}
		}
		
		return argumentTypes;
	}
}
