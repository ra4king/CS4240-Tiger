package edu.cs4240.tiger.util;

import java.util.List;

/**
 * @author Roi Atalla
 */
public class StringifyTigerIR {
	public static String stringifyIR(List<String> ir) {
		String stringified = "";
		
		int line = 1;
		for(String s : ir) {
			stringified += line++ + ":\t";
			if(s.charAt(0) != '.' && s.indexOf(':') == -1) {
				stringified += "\t";
			}
			
			stringified += s + "\n";
		}
		
		return stringified;
	}
}
