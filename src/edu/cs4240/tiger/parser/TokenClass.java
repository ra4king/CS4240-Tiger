package edu.cs4240.tiger.parser;

import edu.cs4240.tiger.regex.Regex;

/**
 * @author Roi Atalla
 */
public enum TokenClass {
	ARRAY(Regex.string("array")),
	BEGIN(Regex.string("begin")),
	BREAK(Regex.string("break")),
	DO(Regex.string("do")),
	ELSE(Regex.string("else")),
	END(Regex.string("end")),
	ENDDO(Regex.string("enddo")),
	ENDIF(Regex.string("endif")),
	FLOAT(Regex.string("float")),
	FOR(Regex.string("for")),
	FUNC(Regex.string("func")),
	IF(Regex.string("if")),
	IN(Regex.string("in")),
	INT(Regex.string("int")),
	LET(Regex.string("let")),
	OF(Regex.string("of")),
	RETURN(Regex.string("return")),
	THEN(Regex.string("then")),
	TO(Regex.string("to")), 
	TYPE(Regex.string("type")),
	VAR(Regex.string("var")),
	WHILE(Regex.string("while")),
	COMMA(Regex.string(",")),
	COLON(Regex.string(":")),
	SEMICOLON(Regex.string(";")),
	LPAREN(Regex.string("(Regex.string(")),
	RPAREN(Regex.string(")")),
	LBRACKET(Regex.string("[")),
	RBRACKET(Regex.string("]")),
	LBRACE(Regex.string("{")),
	RBRACE(Regex.string("}")),
	DOT(Regex.string("\\.")),
	PLUS(Regex.string("\\+")),
	MINUS(Regex.string("-")),
	STAR(Regex.string("\\*")),
	FWSLASH(Regex.string("/")),
	EQUAL(Regex.string("=")),
	NOTEQUAL(Regex.string("<>")), // <>
	LT(Regex.string("<")),
	GT(Regex.string(">")),
	LEQUAL(Regex.string("<=")),
	GEQUAL(Regex.string(">=")),
	AMP(Regex.string("&")),
	PIPE(Regex.string("|")),
	ASSIGN(Regex.string(":=")),
	ID(Regex.and(Regex.or(Regex.letter(), // ([A-Za-z]|_+[A-Za-z0-9])[A-Za-z0-9_]+
	  Regex.and(Regex.oneOrMore(Regex.string("_")), Regex.alphanumeric())),
	  Regex.zeroOrMore(Regex.wordChar()))),
	INTLIT(Regex.oneOrMore(Regex.number())), // [0-9]+
	FLOATLIT(Regex.and(Regex.oneOrMore(Regex.number()), Regex.string("."), Regex.zeroOrMore(Regex.number()))); // [0-9]+\.[0-9]*
	
	private Regex regex;
	
	TokenClass(Regex regex) {
		this.regex = regex;
	}
}
