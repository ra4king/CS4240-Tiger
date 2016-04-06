package edu.cs4240.tiger.parser.node;

import edu.cs4240.tiger.parser.TigerToken;

/**
 * @author Roi Atalla
 */
public class LeafNode implements Node {
	private TigerToken token;
	
	public LeafNode() {
	}
	
	public LeafNode(TigerToken token) {
		this.token = token;
	}
	
	public TigerToken getToken() {
		return token;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof LeafNode) {
			LeafNode l = (LeafNode)o;
			
			return this.token.getTokenClass() == l.getToken().getTokenClass() && this.token.getToken().equals(l.getToken().getToken());
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return token.getToken();
	}
}
