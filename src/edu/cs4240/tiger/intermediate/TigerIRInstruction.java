package edu.cs4240.tiger.intermediate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import edu.cs4240.tiger.intermediate.TigerIROpcode.ParamType;
import edu.cs4240.tiger.util.Pair;

/**
 * @author Roi Atalla
 */
public class TigerIRInstruction {
	private TigerIROpcode opcode;
	private List<Pair<String, ParamType>> params;
	private int lineNumber;
	
	public TigerIRInstruction(TigerIROpcode opcode, List<Pair<String, ParamType>> params, int lineNumber) {
		this.opcode = opcode;
		this.params = params;
		this.lineNumber = lineNumber;
	}
	
	public TigerIROpcode getOpcode() {
		return opcode;
	}
	
	public List<Pair<String, ParamType>> getParams() {
		return params;
	}
	
	public int getLineNumber() {
		return lineNumber;
	}
	
	@Override
	public String toString() {
		String s = "[" + opcode.toString();
		for(Pair<String, ParamType> pair : params) {
			s += ", " + pair.getKey();
		}
		return s + "]";
	}
	
	public static TigerIRInstruction parseInstruction(String[] inst, int lineNumber) {
		TigerIROpcode opcode = null;
		ArrayList<Pair<String, ParamType>> params = new ArrayList<>();
		
		for(TigerIROpcode op : TigerIROpcode.values()) {
			if(op.toString().equals(inst[0])) {
				opcode = op;
				
				int lastI = 0;
				for(int i = 0; i < op.paramTypes.length; i++) {
					ParamType paramType = op.paramTypes[i];
					
					if(paramType == ParamType.OPT_MORE_REGISTERS) {
						while(++i < inst.length) {
							String s = inst[i];
							lastI = i;
							
							Matcher matcher = ParamType.REGISTERi.pattern.matcher(s);
							if(matcher.matches()) {
								params.add(new Pair<>(s, ParamType.REGISTERi));
								continue;
							}
							
							matcher = ParamType.REGISTERf.pattern.matcher(s);
							if(matcher.matches()) {
								params.add(new Pair<>(s, ParamType.REGISTERf));
								continue;
							}
							
							throw new IllegalArgumentException("Invalid parameter '" + s + "' for instruction " + op);
						}
					} else if(paramType == ParamType.OPT_IMMi) {
						if(i + 1 < inst.length) {
							String s = inst[i + 1];
							lastI = i + 1;
							
							Matcher matcher = ParamType.IMMEDIATEi.pattern.matcher(s);
							if(matcher.matches()) {
								params.add(new Pair<>(s, paramType));
							} else {
								throw new IllegalArgumentException("Invalid parameter '" + s + "' for instruction " + op);
							}
						}
					} else if(i + 1 >= inst.length) {
						throw new IllegalArgumentException("Not enough parameters for instruction " + op);
					} else {
						String s = inst[i + 1];
						lastI = i + 1;
						
						Matcher matcher = paramType.pattern.matcher(s);
						if(matcher.matches()) {
							params.add(new Pair<>(s, paramType));
						} else {
							throw new IllegalArgumentException("Invalid parameter '" + s + "' for instruction " + op);
						}
					}
				}
				
				if(lastI + 1 != inst.length) {
					throw new IllegalArgumentException("Too many parameters for instruction " + op);
				}
			}
		}
		
		if(opcode == null) {
			throw new IllegalArgumentException("Unrecognized op " + inst[0]);
		}
		
		return new TigerIRInstruction(opcode, params, lineNumber);
	}
}
