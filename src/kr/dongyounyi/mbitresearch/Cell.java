package kr.dongyounyi.mbitresearch;

import java.util.HashMap;

public class Cell {
	// Constant
	
	// Fields
	private String name;
	private HashMap<String, Pin> pinHash;
	
	// Getter and Setter
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public HashMap<String, Pin> getPinHash() { return pinHash; }
	public void setPinList(HashMap<String, Pin> pinHash) { this.pinHash = pinHash; }
	
	// Constructor
	public Cell() {
		pinHash = new HashMap<String, Pin>();
	}
	public Cell(String cellName) {
		this();
		this.name = cellName;
	}
	
}
