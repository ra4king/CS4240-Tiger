package edu.cs4240.tiger.analyzer;

import edu.cs4240.tiger.parser.TigerToken;
import edu.cs4240.tiger.parser.TigerTokenClass;

/**
 * @author Roi Atalla
 */
public class TigerType {
	public enum BaseType {
		INT,
		FLOAT,
		ARRAY
	}
	
	public static final TigerType INT_TYPE = new TigerType(BaseType.INT);
	public static final TigerType FLOAT_TYPE = new TigerType(BaseType.FLOAT);
	
	public final BaseType baseType;
	
	public TigerType(BaseType baseType) {
		this.baseType = baseType;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TigerType) {
			TigerType t = (TigerType)o;
			return t.baseType == this.baseType;
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
		
		throw new IllegalArgumentException("Argument is not a literal.");
	}
	
	public static boolean isNumericType(TigerType type) {
		return type.equals(TigerType.INT_TYPE) || type.equals(TigerType.FLOAT_TYPE);
	}
	
	@Override
	public String toString() {
		return baseType.toString();
	}
	
	public static class TigerArrayType extends TigerType {
		public final TigerType subType;
		public final int size;
		
		public TigerArrayType(TigerType subType, int size) {
			super(BaseType.ARRAY);
			this.subType = subType;
			this.size = size;
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof TigerArrayType) {
				TigerArrayType t = (TigerArrayType)o;
				return t.baseType == this.baseType && t.subType.equals(this.subType);
			}
			
			return false;
		}
		
		@Override
		public String toString() {
			return super.toString() + "[" + size + "] of " + subType.toString();
		}
	}
}
