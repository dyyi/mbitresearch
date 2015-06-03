package kr.dongyounyi.mbitresearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Verilog {
	// Fields
	private File file;
	private String moduleName;
	private LinkedList<String> pinList;
	private LinkedList<String> inputPinList;
	private LinkedList<String> outputPinList;
	private LinkedList<String> wireList;
	private LinkedList<VerilogComponent> componentList;
	
	
	// Constructor
	public Verilog(String verilogFileName) {
		this(new File(verilogFileName));
	}
	
	public Verilog(File verilogFile) {
		this.file = verilogFile;
		this.pinList = new LinkedList<String>();
		this.inputPinList = new LinkedList<String>();
		this.outputPinList = new LinkedList<String>();
		this.wireList = new LinkedList<String>();
		this.componentList = new LinkedList<VerilogComponent>();
		this.parsing();
	}
	
	public void parsing() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.file));
			String line = null;
			while ((line = br.readLine()) != null) {
				if (!line.matches("(.*);(.*)")) {
					while (!line.matches("(.*);(.*)")) {
						String temp = br.readLine();
						if (temp != null) {
							line += temp;
						} else {
							break;
						}
					}
				}
				// Module name match
				Matcher moduleMatcher = Pattern.compile("^\\s*module\\s+(\\S+)\\s+\\(").matcher(line);
				if (moduleMatcher.find()) {
					this.moduleName = moduleMatcher.group(1);
					Matcher pinMatcher = Pattern.compile("(\\S+),").matcher(line);
					while (pinMatcher.find()) {
						this.pinList.push(pinMatcher.group(1));
					}
					pinMatcher = Pattern.compile("(\\S+)\\s*\\)").matcher(line);
					if (pinMatcher.find()) {
						this.pinList.push(pinMatcher.group(1));
					}
					continue;
				}
				
				// Input pin match
				// Output pin match
				// Component match
				Matcher componentMatcher = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+\\(.*\\);").matcher(line);
				if (componentMatcher.find()) {
					VerilogComponent vc = new VerilogComponent(componentMatcher.group(2), componentMatcher.group(1));
					Matcher pinMatcher = Pattern.compile("\\.(\\S+)\\((\\S+)\\),").matcher(line);
					while (pinMatcher.find()) {
						vc.getPinNetConnectionHash().put(pinMatcher.group(1), pinMatcher.group(2));
					}
					this.componentList.push(vc);
					continue;
				}
				
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Verilog v = new Verilog(args[0]);
	}

}
