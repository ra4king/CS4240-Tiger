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
public enum TigerProductionRule implements TigerClasses {
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
	STMT_IF,
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
	CONST;
	
	public final List<List<TigerClasses>> productions = new ArrayList<>();
	
	static {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(TigerProductionRule.class.getResourceAsStream("ProductionRules.txt"), "UTF-8"));
			
			char arrow = '→';
			String eps = "ϵ";
			
			HashMap<String, TigerTokenClass> specialTokenClasses = new HashMap<>();
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
					System.err.println("No arrow found: " + s);
					continue;
				}
				
				String productionName = s.substring(0, arrowIdx).trim().toUpperCase();
				
				TigerProductionRule productionRule;
				try {
					productionRule = TigerProductionRule.valueOf(productionName);
				}
				catch(Exception exc) {
					System.err.println("Unrecognized production: " + productionName);
					continue;
				}
				
				ArrayList<TigerClasses> productions = new ArrayList<>();
				
				String[] symbols = s.substring(arrowIdx + 1).trim().split(" ");
				for(String symbol : symbols) {
					symbol = symbol.trim();
					if(symbol.isEmpty()) {
						continue;
					}
					
					if(symbol.equals(eps)) {
						break;
					}
					
					symbol = symbol.toUpperCase();
					
					TigerTokenClass specialChar = specialTokenClasses.get(symbol);
					if(specialChar != null) {
						productions.add(specialChar);
						continue;
					}
					
					TigerProductionRule rule;
					try {
						rule = TigerProductionRule.valueOf(symbol);
					}
					catch(Exception exc) {
						TigerTokenClass tokenClass;
						try {
							tokenClass = TigerTokenClass.valueOf(symbol);
						}
						catch(Exception exc2) {
							System.err.println("Unrecognized symbol '" + symbol + "' for rule " + productionName);
							continue;
						}
						
						productions.add(tokenClass);
						continue;
					}
					
					productions.add(rule);
				}
				
				productionRule.productions.add(productions);
			}
		} catch(IOException exc) {
			System.err.println("Error trying to read ProductionRules.txt file.");
			exc.printStackTrace();
		}
	}
}
