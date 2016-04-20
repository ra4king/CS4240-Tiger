package edu.cs4240.tiger.analyzer;

import static edu.cs4240.tiger.util.Utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import edu.cs4240.tiger.analyzer.TigerType.TigerArrayType;
import edu.cs4240.tiger.parser.TigerParseException;
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
public class TigerSymbolTable {
	private RuleNode ast;
	private HashMap<String, TigerType> typeAliases;
	private HashMap<String, Pair<TigerType, String>> variables; // HashMap<Name, Pair<Type, @Nullable InitValue>>
	private HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> functions; // HashMap<Name, Pair<@Nullable ReturnType, List<Pair<ArgName, ArgType>>>>
	public final static HashMap<String, Pair<TigerType, List<Pair<String, TigerType>>>> builtInFunctions;
	
	static {
		builtInFunctions = new HashMap<>();
		builtInFunctions.put("printi", new Pair<>(null, Collections.singletonList(new Pair<>("i", TigerType.INT_TYPE))));
		builtInFunctions.put("printc", new Pair<>(null, Collections.singletonList(new Pair<>("i", TigerType.INT_TYPE))));
		builtInFunctions.put("printf", new Pair<>(null, Collections.singletonList(new Pair<>("f", TigerType.FLOAT_TYPE))));
		builtInFunctions.put("readi", new Pair<>(TigerType.INT_TYPE, Collections.emptyList()));
		builtInFunctions.put("readf", new Pair<>(TigerType.FLOAT_TYPE, Collections.emptyList()));
		
		builtInFunctions.put("srand", new Pair<>(null, Collections.singletonList(new Pair<>("i", TigerType.INT_TYPE))));
		builtInFunctions.put("randi", new Pair<>(TigerType.INT_TYPE, Collections.emptyList()));
		builtInFunctions.put("randf", new Pair<>(TigerType.FLOAT_TYPE, Collections.emptyList()));
		
		builtInFunctions.put("createWindow", new Pair<>(TigerType.INT_TYPE, Arrays.asList(new Pair<>("width", TigerType.INT_TYPE), new Pair<>("height", TigerType.INT_TYPE))));
		builtInFunctions.put("setWindowBackground", new Pair<>(null, Arrays.asList(new Pair<>("id", TigerType.INT_TYPE), new Pair<>("r", TigerType.INT_TYPE), new Pair<>("g", TigerType.INT_TYPE), new Pair<>("b", TigerType.INT_TYPE))));
		builtInFunctions.put("destroyWindow", new Pair<>(null, Collections.singletonList(new Pair<>("id", TigerType.INT_TYPE))));
	}
	
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
	
	public HashMap<String, Pair<TigerType, String>> getVariables() {
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
		if(declseg.size() > 0) {
			buildTypedecls((RuleNode)declseg.get(0));
			buildVardecls((RuleNode)declseg.get(1));
			buildFuncdecls((RuleNode)declseg.get(2));
		}
		
		functions.putAll(builtInFunctions);
	}
	
	private void buildTypedecls(RuleNode typedecls) throws TigerParseException {
		ensureValue(typedecls.getRule(), TigerProductionRule.TYPEDECLS);
		
		if(typedecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode typedecl = (RuleNode)typedecls.getChildren().get(0);
		TigerToken id = ((LeafNode)typedecl.getChildren().get(1)).getToken();
		TigerType type = getBaseType((RuleNode)typedecl.getChildren().get(3));
		
		if(typeAliases.get(id.getTokenString()) != null) {
			throw new TigerParseException("Type previously declared", id);
		}
		
		typeAliases.put(id.getTokenString(), type);
		
		buildTypedecls((RuleNode)typedecls.getChildren().get(1));
	}
	
	private void buildVardecls(RuleNode vardecls) throws TigerParseException {
		ensureValue(vardecls.getRule(), TigerProductionRule.VARDECLS);
		
		if(vardecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode vardecl = (RuleNode)vardecls.getChildren().get(0);
		RuleNode ids = (RuleNode)vardecl.getChildren().get(1);
		TigerType type = getBaseType((RuleNode)vardecl.getChildren().get(3));
		
		String initValue = "";
		
		RuleNode optinit = (RuleNode)vardecl.getChildren().get(4);
		if(optinit.getChildren().size() > 0) {
			TigerToken constToken = ((LeafNode)((RuleNode)optinit.getChildren().get(1)).getChildren().get(0)).getToken();
			TigerType literalType = TigerType.getLiteralType(constToken);
			
			if(!TigerTypeAnalyzer.isTypeCompatibleAssign(type, literalType)) {
				throw new TigerParseException("Incompatible types", constToken);
			}
			
			initValue = constToken.getTokenString();
		}
		
		buildIds(ids, type, initValue);
		
		buildVardecls((RuleNode)vardecls.getChildren().get(1));
	}
	
	private void buildIds(RuleNode ids, TigerType type, String initValue) throws TigerParseException {
		ensureValue(ids.getRule(), TigerProductionRule.IDS);
		
		for(Node child : ids.getChildren()) {
			if(child instanceof RuleNode) {
				buildIds((RuleNode)child, type, initValue);
			} else {
				TigerToken token = ((LeafNode)child).getToken();
				if(token.getTokenClass() == TigerTokenClass.ID) {
					if(variables.get(token.getTokenString()) != null) {
						throw new TigerParseException("Variable previously declared", token);
					}
					
					variables.put(token.getTokenString(), new Pair<>(type, initValue));
				}
			}
		}
	}
	
	private void buildFuncdecls(RuleNode funcdecls) throws TigerParseException {
		ensureValue(funcdecls.getRule(), TigerProductionRule.FUNCDECLS);
		
		if(funcdecls.getChildren().size() == 0) {
			return;
		}
		
		RuleNode funcdecl = (RuleNode)funcdecls.getChildren().get(0);
		TigerToken id = ((LeafNode)funcdecl.getChildren().get(1)).getToken();
		RuleNode params = (RuleNode)funcdecl.getChildren().get(3);
		RuleNode optrettype = (RuleNode)funcdecl.getChildren().get(5);
		
		if(builtInFunctions.get(id.getTokenString()) != null) {
			throw new TigerParseException("Cannot redeclare built-in function", id);
		}
		
		if(functions.get(id.getTokenString()) != null) {
			throw new TigerParseException("Function previously declared", id);
		}
		
		ArrayList<Pair<String, TigerType>> argumentTypes = new ArrayList<>();
		
		if(params.getChildren().size() == 1) {
			buildFuncArgs((RuleNode)params.getChildren().get(0), argumentTypes);
		}
		
		if(optrettype.getChildren().size() != 0) {
			TigerType type = getBaseType((RuleNode)optrettype.getChildren().get(1));
			functions.put(id.getTokenString(), new Pair<>(type, argumentTypes));
		} else {
			functions.put(id.getTokenString(), new Pair<>(null, argumentTypes)); // null == void
		}
		
		buildFuncdecls((RuleNode)funcdecls.getChildren().get(1));
	}
	
	private void buildFuncArgs(RuleNode params, List<Pair<String, TigerType>> argumentTypes) throws TigerParseException {
		ensureValue(params.getRule(), TigerProductionRule.NEPARAMS);
		
		for(Node child : params.getChildren()) {
			if(child instanceof RuleNode) {
				RuleNode ruleNode = (RuleNode)child;
				
				switch(ruleNode.getRule()) {
					case NEPARAMS:
						buildFuncArgs((RuleNode)child, argumentTypes);
						break;
					case PARAM:
						TigerToken id = ((LeafNode)ruleNode.getChildren().get(0)).getToken();
						ensureValue(id.getTokenClass(), TigerTokenClass.ID);
						
						TigerType type = getBaseType((RuleNode)ruleNode.getChildren().get(2));
						
						for(Pair<String, TigerType> pair : argumentTypes) {
							if(pair.getKey().equals(id.getTokenString())) {
								throw new TigerParseException("Argument previously declared", id);
							}
						}
						
						argumentTypes.add(new Pair<>(id.getTokenString(), type));
						break;
					default:
						throw new IllegalArgumentException("Expecting NEPARAMS or PARAM, received " + ruleNode.getRule());
				}
			}
		}
	}
	
	private TigerType getBaseType(RuleNode typeNode) throws TigerParseException {
		ensureValue(typeNode.getRule(), TigerProductionRule.TYPE);
		
		TigerToken token = ((LeafNode)typeNode.getChildren().get(0)).getToken();
		if(token.getTokenClass() == TigerTokenClass.ARRAY) {
			int size = Integer.parseInt(((LeafNode)typeNode.getChildren().get(2)).getToken().getTokenString());
			return new TigerArrayType(getBaseType((RuleNode)typeNode.getChildren().get(5)), size);
		} else if(token.getTokenClass() == TigerTokenClass.ID) {
			TigerType tmp = typeAliases.get(token.getTokenString());
			if(tmp == null) {
				throw new TigerParseException("Unknown type", token);
			}
			return tmp;
		} else {
			return TigerType.getLiteralType(token);
		}
	}
}
