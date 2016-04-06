package edu.cs4240.tiger.parser.node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.cs4240.tiger.parser.TigerProductionRule;

/**
 * @author Roi Atalla
 */
public class RuleNode implements Node {
	private TigerProductionRule value;
	private List<Node> children;
	
	public RuleNode() {
		this((TigerProductionRule)null);
	}
	
	public RuleNode(RuleNode node) {
		this.value = node.value;
		this.children = new ArrayList<>(node.children);
	}
	
	public RuleNode(TigerProductionRule value, Node... children) {
		this.value = value;
		this.children = new ArrayList<>(Arrays.asList(children));
	}
	
	public TigerProductionRule getValue() {
		return value;
	}
	
	public void setValue(TigerProductionRule value) {
		this.value = value;
	}
	
	public List<Node> getChildren() {
		return children;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof RuleNode) {
			RuleNode r = (RuleNode)o;
			
			return this.value == r.getValue() && this.children.equals(r.children);
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		if(children.size() == 0) {
			return value.toString().toLowerCase();
		}
		
		String s = "(" + value.toString().toLowerCase();
		for(Node child : children) {
			s += " " + child.toString();
		}
		s += ")";
		return s;
	}
}
