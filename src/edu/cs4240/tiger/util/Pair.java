package edu.cs4240.tiger.util;

import java.util.List;

/**
 * @author Roi Atalla
 */
public class Pair<K, V> {
	private K key;
	private V value;
	
	public Pair(Pair<K, V> pair) {
		this.key = pair.key;
		this.value = pair.value;
	}
	
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public void setKey(K key) {
		this.key = key;
	}
	
	public V getValue() {
		return value;
	}
	
	public void setValue(V value) {
		this.value = value;
	}
	
	public void set(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	@Override
	public boolean equals(Object other) {
		if(other instanceof Pair) {
			Pair pair = (Pair)other;
			return this.key.equals(pair.key) && this.value.equals(pair.value);
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return key.hashCode() ^ value.hashCode();
	}
	
	@Override
	public String toString() {
		return "(" + key.toString() + " :: " + value.toString() + ")";
	}
	
	public static <K, V> boolean containsKey(List<Pair<K, V>> list, K key) {
		for(Pair<K, V> pair : list) {
			if(pair.key == key || (pair.key != null && key != null && pair.key.equals(key))) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <K, V> boolean containsValue(List<Pair<K, V>> list, V value) {
		for(Pair<K, V> pair : list) {
			if(pair.value == value || (pair.value != null && pair.value.equals(value))) {
				return true;
			}
		}
		
		return false;
	}
}
