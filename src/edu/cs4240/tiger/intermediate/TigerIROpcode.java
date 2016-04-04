package edu.cs4240.tiger.intermediate;

import java.util.regex.Pattern;

/**
 * @author Roi Atalla
 */
public enum TigerIROpcode {
	ADDi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	ADDf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.REGISTERf),
	ADDIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	ADDIf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	SUBi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	SUBf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.REGISTERf),
	SUBIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	SUBIf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	MULi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	MULf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.REGISTERf),
	MULIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	MULIf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	DIVi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	DIVf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.REGISTERi),
	DIVIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	DIVIf(ParamType.REGISTERf, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	AND(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	ANDI(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	OR(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	ORI(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	GTi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	GTf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.REGISTERf),
	GTIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	GTIf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	GEQi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	GEQf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.REGISTERf),
	GEQIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	GEQIf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	EQi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	EQf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.REGISTERf),
	EQIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	EQIf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	NEQi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.REGISTERi),
	NEQf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.REGISTERf),
	NEQIi(ParamType.REGISTERi, ParamType.REGISTERi, ParamType.IMMEDIATEi),
	NEQIf(ParamType.REGISTERi, ParamType.REGISTERf, ParamType.IMMEDIATEf),
	
	LDi(ParamType.REGISTERi, ParamType.LABEL),
	LDf(ParamType.REGISTERf, ParamType.LABEL),
	LDRi(ParamType.REGISTERi, ParamType.REGISTERi),
	LDRf(ParamType.REGISTERf, ParamType.REGISTERi),
	STi(ParamType.REGISTERi, ParamType.LABEL),
	STf(ParamType.REGISTERf, ParamType.LABEL),
	STRi(ParamType.REGISTERi, ParamType.REGISTERi),
	STRf(ParamType.REGISTERf, ParamType.REGISTERi),
	
	BRZ(ParamType.REGISTERi, ParamType.LABEL),
	BRNZ(ParamType.REGISTERi, ParamType.LABEL),
	BR(ParamType.LABEL),
	CALL(ParamType.LABEL, ParamType.OPT_MORE_REGISTERS),
	CALL_RET(ParamType.LABEL, ParamType.REGISTER, ParamType.OPT_MORE_REGISTERS),
	RET(ParamType.OPT_REGISTER);
	
	public final ParamType[] paramTypes;
	
	TigerIROpcode(ParamType... paramTypes) {
		this.paramTypes = paramTypes;
	}
	
	public enum ParamType {
		REGISTERi(Pattern.compile("^\\$i(\\d+)$")),
		REGISTERf(Pattern.compile("^\\$f(\\d+)$")),
		IMMEDIATEi(Pattern.compile("^(\\d+)$")),
		IMMEDIATEf(Pattern.compile("^(\\d+\\.\\d*)$")),
		REGISTER(null),
		OPT_REGISTER(null),
		OPT_MORE_REGISTERS(null),
		LABEL(Pattern.compile("^([A-Za-z_]\\w*)$"));
		
		public Pattern pattern;
		
		ParamType(Pattern pattern) {
			this.pattern = pattern;
		}
	}
}
