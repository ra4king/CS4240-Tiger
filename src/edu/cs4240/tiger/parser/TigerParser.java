package edu.cs4240.tiger.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Roi Atalla
 */
public class TigerParser {
	public static void main(String[] args) {
		for(TigerProductionRule rule : TigerProductionRule.values()) {
			firsts(0, new ArrayList<>(), rule);
		}
	}
	
	private static void firsts(int indent, List<TigerProductionRule> rulesVisited, TigerProductionRule currentRule) {
		while(indent-- > 0) {
			System.out.print("   ");
		}
		
		System.out.print(currentRule);
		
		if(rulesVisited.contains(currentRule)) {
			System.out.println(" - already visited!");
		} else {
			rulesVisited.add(currentRule);
			
			for(List<TigerClasses> classes : currentRule.productions) {
				if(classes.get(0) instanceof TigerProductionRule) {
					firsts(indent + 1, rulesVisited, (TigerProductionRule)classes.get(0));
				}
			}
		}
	}
}
