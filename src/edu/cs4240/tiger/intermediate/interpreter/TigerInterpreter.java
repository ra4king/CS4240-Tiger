package edu.cs4240.tiger.intermediate.interpreter;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import edu.cs4240.tiger.intermediate.TigerIRInstruction;
import edu.cs4240.tiger.intermediate.TigerIROpcode;
import edu.cs4240.tiger.intermediate.TigerIROpcode.ParamType;
import edu.cs4240.tiger.util.Pair;

/**
 * @author Roi Atalla
 */
public class TigerInterpreter {
	private Memory memory;
	private HashMap<String, Pair<List<String>, Integer>> functions;
	private HashMap<String, Integer> labels;
	private ArrayList<TigerIRInstruction> instructions;
	
	public TigerInterpreter(List<String> input) {
		memory = new Memory();
		functions = new HashMap<>();
		labels = new HashMap<>();
		instructions = new ArrayList<>();
		parse(input);
	}
	
	private void parse(List<String> input) {
		int currLine = 1;
		
		for(String s : input) {
			String[] parts = split(s);
			
			try {
				if(parts == null) {
					continue;
				}
				
				switch(parts[0]) {
					case ".VARi":
						if(functions.size() > 0) {
							throw new IllegalArgumentException("Variable declarations come before functions");
						}
						
						if(memory.containsIntVar(parts[1])) {
							throw new IllegalArgumentException("Variable already declared: '" + parts[1] + "'");
						}
						
						if(parts.length == 2) {
							memory.addIntVar(parts[1], 0);
						} else if(parts.length == 3) {
							memory.addIntVar(parts[1], Integer.parseInt(parts[2]));
						} else {
							throw new IllegalArgumentException("Incorrect number of arguments to .VARi");
						}
						break;
					case ".VARf":
						if(functions.size() > 0) {
							throw new IllegalArgumentException("Variable declarations come before functions");
						}
						
						if(memory.containsFloatVar(parts[1])) {
							throw new IllegalArgumentException("Variable already declared: '" + parts[1] + "'");
						}
						
						if(parts.length == 2) {
							memory.addFloatVar(parts[1], 0);
						} else if(parts.length == 3) {
							memory.addFloatVar(parts[1], Float.parseFloat(parts[2]));
						} else {
							throw new IllegalArgumentException("Incorrect number of arguments to .VARf");
						}
						break;
					case ".ARRAYi":
					case ".ARRAYf": {
						if(functions.size() > 0) {
							throw new IllegalArgumentException("Variable declarations come before functions");
						}
						
						if(parts.length <= 2) {
							throw new IllegalArgumentException("Incorrect number of arguments to .ARRAYi");
						}
						
						int[] sizes = new int[parts.length - 2];
						for(int i = 0; i < sizes.length; i++) {
							sizes[i] = Integer.parseInt(parts[i + 2]);
						}
						
						if(parts[0].equals(".ARRAYi")) {
							if(memory.containsIntArray(parts[1])) {
								throw new IllegalArgumentException("Array already declared: '" + parts[1] + "'");
							}
							
							memory.addArray(parts[1], sizes, true);
						} else {
							if(memory.containsFloatArray(parts[1])) {
								throw new IllegalArgumentException("Array already declared: '" + parts[1] + "'");
							}
							
							memory.addArray(parts[1], sizes, false);
						}
						
						break;
					}
					case ".FUNC":
						if(parts.length == 1) {
							throw new IllegalArgumentException("Incorrect number of arguments to .FUNC");
						}
						
						ArrayList<String> args = new ArrayList<>();
						for(int i = 2; i < parts.length; i++) {
							args.add(parts[i]);
						}
						
						functions.put(parts[1], new Pair<>(args, instructions.size()));
						break;
					default:
						if(Pattern.matches("^([A-Za-z_]\\w*):$", parts[0])) {
							if(functions.size() == 0) {
								throw new IllegalArgumentException("Labels go inside functions");
							}
							
							labels.put(parts[0].substring(0, parts[0].length() - 1), instructions.size());
							
							String[] tmp = new String[parts.length - 1];
							System.arraycopy(parts, 1, tmp, 0, tmp.length);
							parts = tmp;
						}
						
						if(parts.length > 0) {
							instructions.add(TigerIRInstruction.parseInstruction(parts, currLine));
							
							if(functions.size() == 0) {
								throw new IllegalArgumentException("Instructions go inside functions");
							}
						}
						
						break;
				}
			}
			catch(Exception exc) {
				throw new IllegalStateException("Error on line " + currLine + ": " + exc.getMessage());
			}
			finally {
				currLine++;
			}
		}
		
		if(functions.get("main") == null) {
			throw new IllegalStateException("No main function found");
		}
		
		if(functions.get("main").getKey().size() > 0) {
			throw new IllegalStateException("Main function cannot have arguments");
		}
	}
	
	public void run(boolean printDebug) {
		if(printDebug) {
			System.out.println(functions);
			System.out.println(labels);
			System.out.println(instructions);
			System.out.println();
		}
		
		Scanner stdin = new Scanner(System.in);
		
		HashMap<String, Integer> intRegs = new HashMap<>();
		HashMap<String, Float> floatRegs = new HashMap<>();
		// Pair<Pair<Return Address, Return Value Register>, HashMap<Argument, Value>>
		Deque<Pair<Pair<Integer, String>, HashMap<String, Number>>> stack = new ArrayDeque<>();
		
		int currentPC = functions.get("main").getValue();
		
		boolean keepRunning = true;
		
		int instrCount = 0;
		
		try {
			while(keepRunning) {
				if(currentPC >= instructions.size()) {
					break;
				}
				
				instrCount++;
				
				TigerIRInstruction currInstr = instructions.get(currentPC++);
				List<Pair<String, ParamType>> params = currInstr.getParams();
				
				try {
					switch(currInstr.getOpcode()) {
						case ADDi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 + src2);
							break;
						}
						case ADDIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 + src2);
							break;
						}
						case ADDf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							floatRegs.put(params.get(0).getKey(), src1 + src2);
							break;
						}
						case ADDIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							floatRegs.put(params.get(0).getKey(), src1 + src2);
							break;
						}
						
						case SUBi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 - src2);
							break;
						}
						case SUBIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 - src2);
							break;
						}
						case SUBf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							floatRegs.put(params.get(0).getKey(), src1 - src2);
							break;
						}
						case SUBIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							floatRegs.put(params.get(0).getKey(), src1 - src2);
							break;
						}
						
						case MULi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 * src2);
							break;
						}
						case MULIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 * src2);
							break;
						}
						case MULf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							floatRegs.put(params.get(0).getKey(), src1 * src2);
							break;
						}
						case MULIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							floatRegs.put(params.get(0).getKey(), src1 * src2);
							break;
						}
						
						case DIVi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 / src2);
							break;
						}
						case DIVIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 / src2);
							break;
						}
						case DIVf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							floatRegs.put(params.get(0).getKey(), src1 / src2);
							break;
						}
						case DIVIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							floatRegs.put(params.get(0).getKey(), src1 / src2);
							break;
						}
						
						case ANDi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 & src2);
							break;
						}
						case ANDIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 & src2);
							break;
						}
						
						case ORi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 | src2);
							break;
						}
						case ORIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 | src2);
							break;
						}
						
						case GTi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 > src2 ? 1 : 0);
							break;
						}
						case GTIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 > src2 ? 1 : 0);
							break;
						}
						case GTf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							intRegs.put(params.get(0).getKey(), src1 > src2 ? 1 : 0);
							break;
						}
						case GTIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 > src2 ? 1 : 0);
							break;
						}
						
						case GEQi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 >= src2 ? 1 : 0);
							break;
						}
						case GEQIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 >= src2 ? 1 : 0);
							break;
						}
						case GEQf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							intRegs.put(params.get(0).getKey(), src1 >= src2 ? 1 : 0);
							break;
						}
						case GEQIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 >= src2 ? 1 : 0);
							break;
						}
						
						case EQi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 == src2 ? 1 : 0);
							break;
						}
						case EQIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 == src2 ? 1 : 0);
							break;
						}
						case EQf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							intRegs.put(params.get(0).getKey(), src1 == src2 ? 1 : 0);
							break;
						}
						case EQIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 == src2 ? 1 : 0);
							break;
						}
						
						case NEQi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = getRegValue(params.get(2).getKey(), intRegs);
							intRegs.put(params.get(0).getKey(), src1 != src2 ? 1 : 0);
							break;
						}
						case NEQIi: {
							int src1 = getRegValue(params.get(1).getKey(), intRegs);
							int src2 = Integer.parseInt(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 != src2 ? 1 : 0);
							break;
						}
						case NEQf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = getRegValue(params.get(2).getKey(), floatRegs);
							intRegs.put(params.get(0).getKey(), src1 != src2 ? 1 : 0);
							break;
						}
						case NEQIf: {
							float src1 = getRegValue(params.get(1).getKey(), floatRegs);
							float src2 = Float.parseFloat(params.get(2).getKey());
							intRegs.put(params.get(0).getKey(), src1 != src2 ? 1 : 0);
							break;
						}
						
						case ITOF: {
							floatRegs.put(params.get(0).getKey(), (float)getRegValue(params.get(1).getKey(), intRegs));
							break;
						}
						
						case LDi: {
							int value;
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.peek();
							if(context != null && context.getValue().containsKey(params.get(1).getKey())) {
								value = context.getValue().get(params.get(1).getKey()).intValue();
							} else {
								value = memory.loadInt(params.get(1).getKey());
							}
							
							intRegs.put(params.get(0).getKey(), value);
							break;
						}
						case LDf: {
							float value;
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.peek();
							if(context != null && context.getValue().containsKey(params.get(1).getKey())) {
								value = context.getValue().get(params.get(1).getKey()).floatValue();
							} else {
								value = memory.loadFloat(params.get(1).getKey());
							}
							
							floatRegs.put(params.get(0).getKey(), value);
							break;
						}
						case LDIi: {
							intRegs.put(params.get(0).getKey(), Integer.parseInt(params.get(1).getKey()));
							break;
						}
						case LDIf: {
							floatRegs.put(params.get(0).getKey(), Float.parseFloat(params.get(1).getKey()));
							break;
						}
						case LDRi: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							intRegs.put(params.get(0).getKey(), memory.loadInt(getRegValue(params.get(1).getKey(), intRegs) + offset));
							break;
						}
						case LDRf: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							floatRegs.put(params.get(0).getKey(), memory.loadFloat(getRegValue(params.get(1).getKey(), intRegs) + offset));
							break;
						}
						
						case STi: {
							int value = getRegValue(params.get(0).getKey(), intRegs);
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.peek();
							if(context != null && context.getValue().containsKey(params.get(1).getKey())) {
								if(context.getValue().get(params.get(1).getKey()).getClass() != Integer.class) {
									throw new IllegalArgumentException("Type mismatch for '" + params.get(1).getKey() + "'");
								}
								context.getValue().put(params.get(1).getKey(), value);
							} else {
								memory.storeInt(params.get(1).getKey(), value);
							}
							break;
						}
						case STf: {
							float value = getRegValue(params.get(0).getKey(), floatRegs);
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.peek();
							if(context != null && context.getValue().containsKey(params.get(1).getKey())) {
								if(context.getValue().get(params.get(1).getKey()).getClass() != Float.class) {
									throw new IllegalArgumentException("Type mismatch for '" + params.get(1).getKey() + "'");
								}
								context.getValue().put(params.get(1).getKey(), value);
							} else {
								memory.storeFloat(params.get(1).getKey(), value);
							}
							break;
						}
						case STIi: {
							memory.storeInt(params.get(1).getKey(), Integer.parseInt(params.get(0).getKey()));
							break;
						}
						case STIf: {
							memory.storeFloat(params.get(1).getKey(), Float.parseFloat(params.get(0).getKey()));
							break;
						}
						case STRi: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							memory.storeInt(getRegValue(params.get(1).getKey(), intRegs) + offset, getRegValue(params.get(0).getKey(), intRegs));
							break;
						}
						case STRf: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							memory.storeFloat(getRegValue(params.get(1).getKey(), intRegs) + offset, getRegValue(params.get(0).getKey(), floatRegs));
							break;
						}
						case STRIi: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							memory.storeInt(getRegValue(params.get(1).getKey(), intRegs) + offset, Integer.parseInt(params.get(0).getKey()));
							break;
						}
						case STRIf: {
							int offset = params.size() == 3 ? Integer.parseInt(params.get(2).getKey()) : 0;
							memory.storeFloat(getRegValue(params.get(1).getKey(), intRegs) + offset, Float.parseFloat(params.get(0).getKey()));
							break;
						}
						
						case BRZ: {
							int src = getRegValue(params.get(0).getKey(), intRegs);
							if(src == 0) {
								if(!labels.containsKey(params.get(1).getKey())) {
									throw new IllegalArgumentException("Invalid label '" + params.get(1).getKey() + "'");
								}
								
								currentPC = labels.get(params.get(1).getKey());
							}
							break;
						}
						case BRNZ: {
							int src = getRegValue(params.get(0).getKey(), intRegs);
							if(src != 0) {
								if(!labels.containsKey(params.get(1).getKey())) {
									throw new IllegalArgumentException("Invalid label '" + params.get(1).getKey() + "'");
								}
								
								currentPC = labels.get(params.get(1).getKey());
							}
							break;
						}
						case BR: {
							if(!labels.containsKey(params.get(0).getKey())) {
								throw new IllegalArgumentException("Invalid label '" + params.get(0).getKey() + "'");
							}
							
							currentPC = labels.get(params.get(0).getKey());
							break;
						}
						
						case CALL: {
							switch(params.get(0).getKey()) {
								case "printi":
									if(params.size() != 2) {
										throw new IllegalArgumentException("Incorrect number of arguments to function 'printi'. Expected 1, got " + (params.size() - 1));
									}
									System.out.println("printi: " + getRegValue(params.get(1).getKey(), intRegs));
									break;
								case "printf":
									if(params.size() != 2) {
										throw new IllegalArgumentException("Incorrect number of arguments to function 'printf'. Expected 1, got " + (params.size() - 1));
									}
									System.out.println("printf: " + getRegValue(params.get(1).getKey(), floatRegs));
									break;
								default:
									if(!functions.containsKey(params.get(0).getKey())) {
										throw new IllegalArgumentException("Unknown function name '" + params.get(0).getKey());
									}
									
									Pair<List<String>, Integer> func = functions.get(params.get(0).getKey());
									if(func.getKey().size() != params.size() - 1) {
										throw new IllegalArgumentException("Incorrect number of arguments to function '" + params.get(0).getKey() +
										                                     "'. Expected " + func.getKey().size() + ", got " + (params.size() - 1));
									}
									
									HashMap<String, Number> funcArgs = new HashMap<>();
									for(int i = 0; i < func.getKey().size(); i++) {
										if(params.get(i + 1).getValue() == ParamType.REGISTERi) {
											funcArgs.put(func.getKey().get(i), getRegValue(params.get(i + 1).getKey(), intRegs));
										} else {
											funcArgs.put(func.getKey().get(i), getRegValue(params.get(i + 1).getKey(), floatRegs));
										}
									}
									stack.push(new Pair<>(new Pair<>(currentPC, null), funcArgs));
									currentPC = func.getValue();
									break;
							}
							break;
						}
						case CALL_RET: {
							switch(params.get(0).getKey()) {
								case "readi":
									if(params.size() != 2) {
										throw new IllegalArgumentException("Incorrect number of arguments to function 'printi'. Expected 0, got " + (params.size() - 2));
									}
									
									System.out.print("readi: ");
									if(!stdin.hasNextInt()) {
										throw new IllegalStateException("Type mismatch, readi expected integer");
									}
									
									intRegs.put(params.get(1).getKey(), stdin.nextInt());
									break;
								case "readf":
									if(params.size() != 2) {
										throw new IllegalArgumentException("Incorrect number of arguments to function 'printf'. Expected 0, got " + (params.size() - 2));
									}
									
									System.out.print("readf: ");
									if(!stdin.hasNextFloat()) {
										throw new IllegalStateException("Type mismatch, readf expected float");
									}
									
									floatRegs.put(params.get(1).getKey(), stdin.nextFloat());
									break;
								default:
									if(!functions.containsKey(params.get(0).getKey())) {
										throw new IllegalArgumentException("Unknown function name '" + params.get(0).getKey());
									}
									
									Pair<List<String>, Integer> func = functions.get(params.get(0).getKey());
									if(func.getKey().size() != params.size() - 2) {
										throw new IllegalArgumentException("Incorrect number of arguments to function '" + params.get(0).getKey() +
										                                     "'. Expected " + func.getKey().size() + ", got " + (params.size() - 2));
									}
									
									HashMap<String, Number> funcArgs = new HashMap<>();
									for(int i = 0; i < func.getKey().size(); i++) {
										if(params.get(i + 2).getValue() == ParamType.REGISTERi) {
											funcArgs.put(func.getKey().get(i), getRegValue(params.get(i + 2).getKey(), intRegs));
										} else {
											funcArgs.put(func.getKey().get(i), getRegValue(params.get(i + 2).getKey(), floatRegs));
										}
									}
									stack.push(new Pair<>(new Pair<>(currentPC, params.get(1).getKey()), funcArgs));
									currentPC = func.getValue();
									break;
							}
							break;
						}
						case RET: {
							if(stack.size() == 0) {
								keepRunning = false;
								break;
							}
							
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.pop();
							if(context.getKey().getValue() != null) {
								throw new IllegalArgumentException("Expected return value");
							}
							
							currentPC = context.getKey().getKey();
							break;
						}
						case RETi:
						case RETf: {
							if(stack.size() == 0) {
								throw new IllegalArgumentException("Cannot return value from main");
							}
							
							Pair<Pair<Integer, String>, HashMap<String, Number>> context = stack.pop();
							if(context.getKey().getValue() != null) {
								if(currInstr.getOpcode() == TigerIROpcode.RETi) {
									if(context.getKey().getValue().charAt(1) == 'i') {
										intRegs.put(context.getKey().getValue(), getRegValue(params.get(0).getKey(), intRegs));
									} else {
										floatRegs.put(context.getKey().getValue(), (float)getRegValue(params.get(0).getKey(), intRegs));
									}
								} else {
									if(context.getKey().getValue().charAt(1) != 'f') {
										throw new IllegalArgumentException("Type mismatch on return value, callsite expected int");
									}
									
									floatRegs.put(context.getKey().getValue(), getRegValue(params.get(0).getKey(), floatRegs));
								}
							}
							
							currentPC = context.getKey().getKey();
							break;
						}
					}
				}
				catch(Exception exc) {
					throw new IllegalStateException("Error on line " + currInstr.getLineNumber() + ": " + exc.getMessage(), exc);
				}
			}
		}
		finally {
			if(printDebug) {
				System.out.println("\n===============================================");
				System.out.println("DEBUG SUMMARY:");
				
				System.out.println("\nExecuted " + instrCount + " instructions.");
				
				System.out.println("\nInt regs:");
				
				for(String s : intRegs.keySet().stream().sorted((s1, s2) -> Integer.parseInt(s1.substring(2)) - Integer.parseInt(s2.substring(2))).collect(Collectors.toList())) {
					System.out.println(s + ": " + intRegs.get(s));
				}
				
				System.out.println("\nFloat regs:");
				for(String s : floatRegs.keySet().stream().sorted((s1, s2) -> Integer.parseInt(s1.substring(2)) - Integer.parseInt(s2.substring(2))).collect(Collectors.toList())) {
					System.out.println(s + ": " + floatRegs.get(s));
				}
				
				memory.printMemory();
			}
		}
	}
	
	private static <T> T getRegValue(String name, HashMap<String, T> regs) {
		if(regs.containsKey(name)) {
			return regs.get(name);
		}
		
		throw new IllegalArgumentException("Register '" + name + "' does not exist.");
	}
	
	private static String[] split(String inst) {
		if(inst.trim().isEmpty()) {
			return null;
		}
		
		String[] p = inst.replace(',', ' ').split("\\s");
		ArrayList<String> parts = new ArrayList<>();
		for(String s : p) {
			String t = s.trim();
			if(!t.isEmpty()) {
				parts.add(t);
			}
		}
		return parts.toArray(new String[parts.size()]);
	}
}
