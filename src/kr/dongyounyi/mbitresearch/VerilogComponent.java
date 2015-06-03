package kr.dongyounyi.mbitresearch;

import java.util.HashMap;

public class VerilogComponent {
	// Fields
	private String instanceName;
	private String cellName;
	private HashMap<String, String> pinNetConnectionHash;
	
	// Setter and Getter	
	public String getInstanceName() { return instanceName; }
	public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
	public String getCellName() { return cellName; }
	public void setCellName(String cellName) { this.cellName = cellName; }
	public HashMap<String, String> getPinNetConnectionHash() { return pinNetConnectionHash;	}
	public void setPinNetConnectionHash(HashMap<String, String> pinNetConnectionHash) {	this.pinNetConnectionHash = pinNetConnectionHash; }
	
	// Constructor
	public VerilogComponent() {
		pinNetConnectionHash = new HashMap<String, String>();
	}
	
	public VerilogComponent(String instanceName, String cellName) {
		this.instanceName = instanceName;
		this.cellName = cellName;
		pinNetConnectionHash = new HashMap<String, String>();
	}
}
