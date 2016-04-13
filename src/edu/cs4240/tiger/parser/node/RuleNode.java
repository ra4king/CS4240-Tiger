package edu.cs4240.tiger.parser.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cs4240.tiger.parser.TigerProductionRule;

/**
 * @author Roi Atalla
 */
public class RuleNode implements Node {
	private TigerProductionRule rule;
	private List<Node> children;
	
	public RuleNode() {
		this((TigerProductionRule)null);
	}
	
	public RuleNode(RuleNode node) {
		this.rule = node.rule;
		this.children = new ArrayList<>(node.children);
	}
	
	public RuleNode(TigerProductionRule rule, Node... children) {
		this.rule = rule;
		this.children = new ArrayList<>(Arrays.asList(children));
	}
	
	public TigerProductionRule getRule() {
		return rule;
	}
	
	public void setRule(TigerProductionRule rule) {
		this.rule = rule;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof RuleNode) {
			RuleNode r = (RuleNode)o;
			
			return this.rule == r.getRule() && this.children.equals(r.children);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) {
			return rule.toString().toLowerCase();
		}
		
		String s = "(" + rule.toString().toLowerCase();
		for(Node child : children) {
			s += " " + child.toString();
		}
		s += ")";
		return s;
	}
}
