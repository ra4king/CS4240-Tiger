package edu.cs4240.tiger.parser;

/**
 * @author Roi Atalla
 */
public enum TigerProductionRule implements TigerClasses {
	PROGRAM(array(TigerTokenClass.LET, TigerProductionRule.DECLSEG, TigerTokenClass.IN, TigerProductionRule.STMTS, TigerTokenClass.END)),
	DECLSEG(array(TigerProductionRule.TYPEDECLS, TigerProductionRule.VARDECLS)),
	TYPEDECLS(array(), array(TigerProductionRule.TYPEDECL, TigerProductionRule.TYPEDECLS)),
	TYPEDECL(array(TigerProductionRule.TYPE, TigerTokenClass.ID, TigerTokenClass.ASSIGN, TigerProductionRule.TYPE)),
	TYPE(array(TigerTokenClass.INT), array(TigerTokenClass.FLOAT), array(TigerTokenClass.ID),
	      array(TigerTokenClass.ARRAY, TigerTokenClass.LBRACKET, TigerTokenClass.INTLIT, TigerTokenClass.RBRACKET, TigerTokenClass.OF, TigerTokenClass.TYPE)),
	VARDECLS(array(), array(TigerProductionRule.VARDECL, TigerProductionRule.VARDECLS)),
	VARDECL(array(TigerTokenClass.VAR, TigerProductionRule.IDS, TigerTokenClass.COLON, TigerProductionRule.TYPE, TigerProductionRule.OPTINIT, TigerTokenClass.SEMICOLON)),
	IDS(array(TigerTokenClass.ID, TigerProductionRule.IDS_TAIL)),
	IDS_TAIL(array(), array(TigerTokenClass.COMMA, TigerProductionRule.IDS)),
	OPTINIT(array(), array(TigerTokenClass.ASSIGN, TigerProductionRule.CONST)),
	FUNCDECLS(array(), array(TigerProductionRule.FUNCDECL, TigerProductionRule.FUNCDECLS)),
	FUNCDECL(array(TigerTokenClass.FUNC, TigerTokenClass.ID, TigerTokenClass.LPAREN, TigerProductionRule.PARAMS, TigerTokenClass.RPAREN,
	  TigerProductionRule.OPTRETTYPE, TigerTokenClass.BEGIN, TigerProductionRule.STMTS, TigerTokenClass.END, TigerTokenClass.SEMICOLON)),
	PARAMS(array(), array(TigerProductionRule.NEPARAMS)),
	NEPARAMS(array(TigerProductionRule.PARAM, TigerProductionRule.NEPARAMS_TAIL)),
	NEPARAMS_TAIL(array(), array(TigerTokenClass.COMMA, TigerProductionRule.NEPARAMS)),
	PARAM(array(TigerTokenClass.ID, TigerTokenClass.COLON, TigerTokenClass.TYPE)),
	OPTRETTYPE(array(), array(TigerTokenClass.COLON, TigerTokenClass.TYPE)),
	STMTS(array(TigerProductionRule.FULLSTMT, TigerProductionRule.STMTS_TAIL)),
	STMTS_TAIL(array(), array(TigerProductionRule.FULLSTMT, TigerProductionRule.STMTS)),
	FULLSTMT(array(TigerProductionRule.STMT, TigerTokenClass.SEMICOLON)),
	STMT(array(TigerProductionRule.LVALUE, TigerTokenClass.ASSIGN, TigerProductionRule.NUMEXPR),
	      array(TigerTokenClass.IF, TigerProductionRule.BOOLEXPR, TigerTokenClass.THEN, TigerProductionRule.STMTS, TigerProductionRule.STMT_IF),
	      array(TigerTokenClass.IF, TigerProductionRule.BOOLEXPR, TigerTokenClass.THEN, TigerProductionRule.STMTS, TigerProductionRule.STMT_IF),
	      array(TigerTokenClass.WHILE, TigerProductionRule.BOOLEXPR, TigerTokenClass.DO, TigerProductionRule.STMTS, TigerTokenClass.ENDDO),
	      array(TigerTokenClass.FOR, TigerTokenClass.ID, TigerTokenClass.ASSIGN, TigerProductionRule.NUMEXPR, TigerTokenClass.TO, TigerProductionRule.NUMEXPR,
		    TigerTokenClass.DO, TigerProductionRule.STMTS, TigerTokenClass.ENDDO),
	      array(TigerProductionRule.OPTSTORE, TigerTokenClass.ID, TigerTokenClass.LPAREN, TigerProductionRule.NUMEXPRS, TigerTokenClass.RPAREN),
	      array(TigerTokenClass.BREAK), array(TigerTokenClass.RETURN, TigerProductionRule.NUMEXPR)),
	STMT_IF(array(TigerTokenClass.ENDIF), array(TigerTokenClass.ELSE, TigerProductionRule.STMTS, TigerTokenClass.ENDIF)),
	LVALUE(array(TigerTokenClass.ID, TigerProductionRule.OPTOFFSET)),
	OPTOFFSET(array(), array(TigerTokenClass.LBRACKET, TigerProductionRule.NUMEXPR, TigerTokenClass.RBRACKET)),
	OPTSTORE(array(), array(TigerProductionRule.LVALUE, TigerTokenClass.ASSIGN)),
	NUMEXPRS(array(), array(TigerProductionRule.NEEXPRS)),
	NEEXPRS(array(TigerProductionRule.NUMEXPR, TigerProductionRule.NEEXPRS_TAIL)),
	NEEXPRS_TAIL(array(), array(TigerTokenClass.COMMA, TigerProductionRule.NEEXPRS)),
	BOOLEXPR(array(TigerProductionRule.CLAUSE, TigerProductionRule.BOOLEXPR_TAIL)),
	BOOLEXPR_TAIL(array(), array(TigerTokenClass.PIPE, TigerProductionRule.BOOLEXPR)),
	CLAUSE(array(TigerProductionRule.PRED, TigerProductionRule.CLAUSE_TAIL)),
	CLAUSE_TAIL(array(), array(TigerTokenClass.AMP, TigerProductionRule.CLAUSE)),
	PRED(array(TigerProductionRule.NUMEXPR, TigerProductionRule.BOOLOP, TigerProductionRule.NUMEXPR),
	      array(TigerTokenClass.LPAREN, TigerProductionRule.BOOLEXPR, TigerTokenClass.RPAREN)),
	BOOLOP(array(TigerTokenClass.EQUAL), array(TigerTokenClass.NOTEQUAL), array(TigerTokenClass.LEQUAL),
	        array(TigerTokenClass.GEQUAL), array(TigerTokenClass.LT), array(TigerTokenClass.GT)),
	NUMEXPR(array(TigerProductionRule.TERM, TigerProductionRule.NUMEXPR_TAIL)),
	NUMEXPR_TAIL(array(), array(TigerProductionRule.LINOP, TigerProductionRule.NUMEXPR)),
	LINOP(array(TigerTokenClass.PLUS), array(TigerTokenClass.MINUS)),
	TERM(array(TigerProductionRule.FACTOR, TigerProductionRule.TERM_TAIL)),
	TERM_TAIL(array(), array(TigerProductionRule.NONLINOP, TigerProductionRule.TERM)),
	NONLINOP(array(TigerTokenClass.MULT), array(TigerTokenClass.DIV)),
	FACTOR(array(TigerProductionRule.CONST), array(TigerTokenClass.ID, TigerProductionRule.OPTOFFSET),
	        array(TigerTokenClass.LPAREN, TigerProductionRule.NUMEXPR, TigerTokenClass.RPAREN)),
	CONST(array(TigerTokenClass.INTLIT), array(TigerTokenClass.FLOATLIT));
	
	public final TigerClasses[][] productions;
	
	TigerProductionRule(TigerClasses[] ... productions) {
		this.productions = productions;
	}
	
	private static TigerClasses[] array(TigerClasses ... classes) {
		return classes;
	}
}
