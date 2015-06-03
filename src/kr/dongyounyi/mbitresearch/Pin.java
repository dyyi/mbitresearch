package kr.dongyounyi.mbitresearch;

public class Pin {
	// CONST
	public static String INPUT = "INPUT";
	public static String OUTPUT = "OUTPUT";
	public static String INOUT = "INOUT";
	public static String FEEDTHRU = "FEEDTHRU";
	public static String SIGNAL = "SIGNAL";
	public static String ANALOG = "ANALOG";
	public static String POWER = "POWER";
	public static String GROUND = "GROUND";
	public static String CLOCK = "CLOCK";
	
	// Fields
	private String name;
	private String direction;
	private String use;
	
	// Getter and Setter
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public String getDirection() { return direction; }
	public void setDirection(String direction) { this.direction = direction; }
	public String getUse() { return use; }
	public void setUse(String use) { this.use = use; }
	
	// Constructor
	public Pin() {;}
	public Pin(String name) { this.name = name; }
}
