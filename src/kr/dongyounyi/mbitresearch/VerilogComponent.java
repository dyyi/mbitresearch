package kr.dongyounyi.mbitresearch;

import java.util.HashMap;

public class VerilogComponent {
	// Fields
	private String instanceName;
	private String cellName;
	private HashMap<String, String> connectionHash; // Pin, Net
	
	// Setter and Getter	
	public String getInstanceName() { return instanceName; }
	public void setInstanceName(String instanceName) { this.instanceName = instanceName; }
	public String getCellName() { return cellName; }
	public void setCellName(String cellName) { this.cellName = cellName; }
	public HashMap<String, String> getConnectionHash() { return connectionHash;	}
	public void setConnectionHash(HashMap<String, String> connectionHash) {	this.connectionHash = connectionHash; }
	
	// Constructor
	public VerilogComponent() {
		connectionHash = new HashMap<String, String>();
	}
	
	public VerilogComponent(String instanceName, String cellName) {
		this.instanceName = instanceName;
		this.cellName = cellName;
		connectionHash = new HashMap<String, String>();
	}
}
