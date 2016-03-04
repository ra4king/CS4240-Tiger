package edu.cs4240.tiger.analyzer;

import java.util.HashMap;
import java.util.List;

import edu.cs4240.tiger.parser.TigerParser.LeafNode;
import edu.cs4240.tiger.parser.TigerParser.Node;
import edu.cs4240.tiger.parser.TigerParser.RuleNode;
import edu.cs4240.tiger.parser.TigerProductionRule;
import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerAnalyzer {
	private RuleNode ast;
	private HashMap<String, RuleNode> typeAliases;
	private HashMap<String, RuleNode> varTypes;
	private HashMap<String, RuleNode> funcTypes;
	
	public TigerAnalyzer(RuleNode ast) {
		this.ast = ast;
		
		buildSymbolTable();
		
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
		RuleNode optrettype = (RuleNode)funcdecl.getChildren().get(5);
		
		if(optrettype.getChildren().size() != 0) {
			funcTypes.put(id.getToken(), (RuleNode)optrettype.getChildren().get(1));
		} else {
			funcTypes.put(id.getToken(), null); // null == void
		}
		
		analyzeFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
}
