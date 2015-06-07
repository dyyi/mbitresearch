package kr.dongyounyi.mbitresearch;

import java.util.HashMap;
import java.util.LinkedList;

public class Net {
	// Fields
	private String name;
	private HashMap<LogicComponent, LinkedList<Pin>> connectionHash;
	
	// Getter and Setter
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public HashMap<LogicComponent, LinkedList<Pin>> getConnectionHash() { return connectionHash; }
	public void setConnectionHash(HashMap<LogicComponent, LinkedList<Pin>> connectionHash) { this.connectionHash = connectionHash; }
	
	// Constructor
	public Net() {
		connectionHash = new HashMap<LogicComponent, LinkedList<Pin>>();
	}
	public Net(String name) {
		this();
		this.name = name;
	}
	
	// Methods
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass().equals(this.getClass())) {
			return ((Cell)obj).getName().equals(this.getName());
		} else {
			return false;
		}
	}
}
