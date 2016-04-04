package edu.cs4240.tiger.intermediate.interpreter;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Roi Atalla
 */
public class Memory {
	private HashMap<String, Integer> intVars;
	private HashMap<String, Float> floatVars;
	
	private ArrayList<int[]> intArrays;
	private ArrayList<float[]> floatArrays;
	
	public Memory() {
		intVars = new HashMap<>();
		floatVars = new HashMap<>();
		intArrays = new ArrayList<>();
		floatArrays = new ArrayList<>();
	}
	
	public void printMemory() {
		System.out.print("Int vars: ");
		System.out.println(intVars);
		
		System.out.print("\nFloat vars: ");
		System.out.println(floatVars);
		
		System.out.println("\nInt memory:");
		for(int i = 0, currAddr = 0; i < intArrays.size(); i++) {
			for(int j = 0; j < intArrays.get(i).length; j++, currAddr++) {
				System.out.println(currAddr + ": " + intArrays.get(i)[j]);
			}
		}
		
		System.out.println("\nFloat memory:");
		for(int i = 0, currAddr = 0; i < floatArrays.size(); i++) {
			for(int j = 0; j < floatArrays.get(i).length; j++, currAddr++) {
				System.out.println(currAddr + ": " + floatArrays.get(i)[j]);
			}
		}
		
		System.out.println();
	}
	
	public void addIntVar(String name, int value) {
		intVars.put(name, value);
	}
	
	public void addFloatVar(String name, float value) {
		floatVars.put(name, value);
	}
	
	public void addIntArray(String name, int[] sizes) {
		intVars.put(name, addIntArray(sizes, 0));
	}
	
	private int addIntArray(int[] sizes, int sizesIdx) {
		int currAddr = 0;
		for(int[] a : intArrays) {
			currAddr += a.length;
		}
		
		int[] arr = new int[sizes[sizesIdx]];
		intArrays.add(arr);
		
		if(sizesIdx + 1 < sizes.length) {
			for(int i = 0; i < arr.length; i++) {
				arr[i] = addIntArray(sizes, sizesIdx + 1);
			}
		}
		
		return currAddr;
	}
	
	public void addFloatArray(String name, int[] sizes) {
		intVars.put(name, addFloatArray(sizes, 0));
	}
	
	private int addFloatArray(int[] sizes, int sizesIdx) {
		int currAddr = 0;
		for(float[] a : floatArrays) {
			currAddr += a.length;
		}
		
		float[] arr = new float[sizes[sizesIdx]];
		floatArrays.add(arr);
		
		if(sizesIdx + 1 < sizes.length) {
			for(int i = 0; i < arr.length; i++) {
				arr[i] = addFloatArray(sizes, sizesIdx + 1);
			}
		}
		
		return currAddr;
	}
	
	public int loadInt(int address) {
		for(int[] arr : intArrays) {
			if(address >= arr.length) {
				address -= arr.length;
			} else {
				return arr[address];
			}
		}
		
		throw new IllegalArgumentException("Invalid address '" + address + "'");
	}
	
	public int loadInt(String name) {
		if(intVars.containsKey(name)) {
			return intVars.get(name);
		}
		
		throw new IllegalArgumentException("Invalid variable name '" + name + "'");
	}
	
	public float loadFloat(int address) {
		for(float[] arr : floatArrays) {
			if(address >= arr.length) {
				address -= arr.length;
			} else {
				return arr[address];
			}
		}
		
		throw new IllegalArgumentException("Invalid address '" + address + "'");
	}
	
	public float loadFloat(String name) {
		if(floatVars.containsKey(name)) {
			return floatVars.get(name);
		}
		
		throw new IllegalArgumentException("Invalid variable name '" + name + "'");
	}
	
	public void storeInt(int address, int value) {
		for(int[] arr : intArrays) {
			if(address >= arr.length) {
				address -= arr.length;
			} else {
				arr[address] = value;
				return;
			}
		}
		
		throw new IllegalArgumentException("Invalid address '" + address + "'");
	}
	
	public void storeInt(String name, int value) {
		if(intVars.containsKey(name)) {
			intVars.put(name, value);
			return;
		}
		
		throw new IllegalArgumentException("Invalid variable name '" + name + "'");
	}
	
	public void storeFloat(int address, float value) {
		for(float[] arr : floatArrays) {
			if(address >= arr.length) {
				address -= arr.length;
			} else {
				arr[address] = value;
				return;
			}
		}
		
		throw new IllegalArgumentException("Invalid address '" + address + "'");
	}
	
	public void storeFloat(String name, float value) {
		if(floatVars.containsKey(name)) {
			floatVars.put(name, value);
			return;
		}
		
		throw new IllegalArgumentException("Invalid variable name '" + name + "'");
	}
}
