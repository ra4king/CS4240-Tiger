package edu.cs4240.tiger.analyzer;

import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerType {
	public enum Type {
		INT,
		FLOAT,
		BOOL,
		ARRAY
	}
	
	public static final TigerType INT_TYPE = new TigerType(Type.INT);
	public static final TigerType FLOAT_TYPE = new TigerType(Type.FLOAT);
	public static final TigerType BOOL_TYPE = new TigerType(Type.BOOL);
	
	public final Type type;
	
	public TigerType(Type type) {
		this.type = type;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TigerType) {
			TigerType t = (TigerType)o;
			return t.type == this.type;
		}
		
		return false;
	}
	
	public static TigerType getLiteralType(TigerToken token) {
		if(token.getTokenClass() == TigerTokenClass.INT || token.getTokenClass() == TigerTokenClass.INTLIT) {
			return INT_TYPE;
		}
		if(token.getTokenClass() == TigerTokenClass.FLOAT || token.getTokenClass() == TigerTokenClass.FLOATLIT) {
			return FLOAT_TYPE;
		}
		if(token.getTokenClass() == TigerTokenClass.BOOL || token.getTokenClass() == TigerTokenClass.TRUE || token.getTokenClass() == TigerTokenClass.FALSE) {
			return BOOL_TYPE;
		}
		
		throw new IllegalArgumentException("Argument is not a literal.");
	}
	
	public static boolean isNumericType(TigerType type) {
		return type.equals(TigerType.INT_TYPE) || type.equals(TigerType.FLOAT_TYPE);
	}
	
	public static class TigerArrayType extends TigerType {
		public final TigerType subtype;
		
		public TigerArrayType(TigerType subtype) {
			super(Type.ARRAY);
			this.subtype = subtype;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof TigerArrayType) {
				TigerArrayType t = (TigerArrayType)o;
				return t.type == this.type && t.subtype.equals(this.subtype);
			}
			
			return false;
		}
	}
}
