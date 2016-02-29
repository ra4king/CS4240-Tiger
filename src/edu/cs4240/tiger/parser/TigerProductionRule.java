package edu.cs4240.tiger.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Roi Atalla
 */
public enum TigerProductionRule implements TigerSymbol {
	PROGRAM,
	DECLSEG,
	TYPEDECLS,
	TYPEDECL,
	TYPE,
	VARDECLS,
	VARDECL,
	IDS,
	IDS_TAIL,
	OPTINIT,
	FUNCDECLS,
	FUNCDECL,
	PARAMS,
	NEPARAMS,
	NEPARAMS_TAIL,
	PARAM,
	OPTRETTYPE,
	STMTS,
	STMTS_TAIL,
	FULLSTMT,
	STMT,
	STMT_TAIL,
	LVALUE,
	OPTOFFSET,
	OPTSTORE,
	NUMEXPRS,
	NEEXPRS,
	NEEXPRS_TAIL,
	BOOLEXPR,
	BOOLEXPR_TAIL,
	CLAUSE,
	CLAUSE_TAIL,
	PRED,
	BOOLOP,
	NUMEXPR,
	NUMEXPR_TAIL,
	LINOP,
	TERM,
	TERM_TAIL,
	NONLINOP,
	FACTOR,
	FACTOR_TAIL,
	CONST;
	
	public final List<List<TigerSymbol>> productions = new ArrayList<>();
	
	private static HashMap<String, TigerTokenClass> specialTokenClasses = new HashMap<>();
	
	public static String printRule(TigerProductionRule rule, List<TigerSymbol> symbols) {
		char arrow = '→';
		char eps = 'ϵ';
		
		String s = rule.toString().toLowerCase() + " " + arrow;
		
		if(symbols.isEmpty()) {
			s += " " + eps;
		} else {
			for(TigerSymbol c : symbols) {
				if(c instanceof TigerTokenClass && specialTokenClasses.containsValue(c)) {
					for(String token : specialTokenClasses.keySet()) {
						if(specialTokenClasses.get(token) == c) {
							s += " " + token;
							break;
						}
					}
				} else {
					s += " " + c.toString().toLowerCase();
				}
			}
		}
		
		return s;
	}
	
	static {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(TigerProductionRule.class.getResourceAsStream("ProductionRules.txt"), "UTF-8"));
			
			char arrow = '→';
			
			specialTokenClasses.put("ϵ", TigerTokenClass.EPSILON);
			specialTokenClasses.put(",", TigerTokenClass.COMMA);
			specialTokenClasses.put(":", TigerTokenClass.COLON);
			specialTokenClasses.put(";", TigerTokenClass.SEMICOLON);
			specialTokenClasses.put("(", TigerTokenClass.LPAREN);
			specialTokenClasses.put(")", TigerTokenClass.RPAREN);
			specialTokenClasses.put("[", TigerTokenClass.LBRACKET);
			specialTokenClasses.put("]", TigerTokenClass.RBRACKET);
			specialTokenClasses.put(".", TigerTokenClass.DOT);
			specialTokenClasses.put("+", TigerTokenClass.PLUS);
			specialTokenClasses.put("-", TigerTokenClass.MINUS);
			specialTokenClasses.put("*", TigerTokenClass.MULT);
			specialTokenClasses.put("/", TigerTokenClass.DIV);
			specialTokenClasses.put("=", TigerTokenClass.EQUAL);
			specialTokenClasses.put("<>", TigerTokenClass.NOTEQUAL);
			specialTokenClasses.put("<", TigerTokenClass.LT);
			specialTokenClasses.put(">", TigerTokenClass.GT);
			specialTokenClasses.put("<=", TigerTokenClass.LEQUAL);
			specialTokenClasses.put(">=", TigerTokenClass.GEQUAL);
			specialTokenClasses.put("&", TigerTokenClass.AMP);
			specialTokenClasses.put("|", TigerTokenClass.PIPE);
			specialTokenClasses.put(":=", TigerTokenClass.ASSIGN);
			
			String s;
			while((s = reader.readLine()) != null) {
				int arrowIdx = s.indexOf(arrow);
				if(arrowIdx == -1) {
					throw new RuntimeException("No arrow found: " + s);
				}
				
				String productionName = s.substring(0, arrowIdx).trim().toUpperCase();
				
				TigerProductionRule productionRule;
				try {
					productionRule = TigerProductionRule.valueOf(productionName);
				}
				catch(Exception exc) {
					throw new RuntimeException("Unrecognized production: " + productionName);
				}
				
				ArrayList<TigerSymbol> productions = new ArrayList<>();
				
				String[] symbols = s.substring(arrowIdx + 1).trim().split(" ");
				for(String symbol : symbols) {
					symbol = symbol.trim();
					if(symbol.isEmpty()) {
						continue;
					}
					
					TigerTokenClass specialChar = specialTokenClasses.get(symbol);
					if(specialChar != null) {
						productions.add(specialChar);
						continue;
					}
					
					symbol = symbol.toUpperCase();
					
					try {
						TigerProductionRule rule = TigerProductionRule.valueOf(symbol);
						productions.add(rule);
					}
					catch(Exception exc) {
						TigerTokenClass tokenClass;
						try {
							tokenClass = TigerTokenClass.valueOf(symbol);
							productions.add(tokenClass);
						}
						catch(Exception exc2) {
							throw new RuntimeException("Unrecognized symbol '" + symbol + "' for rule " + productionName);
						}
					}
				}
				
				productionRule.productions.add(productions);
			}
			
			for(TigerProductionRule rule : TigerProductionRule.values()) {
				if(rule.productions.isEmpty())
					throw new RuntimeException("Empty production rule: " + rule);
			}
		}
		catch(IOException exc) {
			throw new RuntimeException("Error trying to read ProductionRules.txt file.", exc);
		}
	}
}
