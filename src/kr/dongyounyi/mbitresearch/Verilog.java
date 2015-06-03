package kr.dongyounyi.mbitresearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
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
	private HashMap<String, String> assignHash;


	// Getter and Setter
	public File getFile() { return file; }
	public void setFile(File file) { this.file = file; }
	public String getModuleName() { return moduleName; }
	public void setModuleName(String moduleName) { this.moduleName = moduleName; }
	public LinkedList<String> getPinList() { return pinList; }
	public void setPinList(LinkedList<String> pinList) { this.pinList = pinList; }
	public LinkedList<String> getInputPinList() { return inputPinList;}
	public void setInputPinList(LinkedList<String> inputPinList) { this.inputPinList = inputPinList; }
	public LinkedList<String> getOutputPinList() { return outputPinList; }
	public void setOutputPinList(LinkedList<String> outputPinList) { this.outputPinList = outputPinList; }
	public LinkedList<String> getWireList() { return wireList; }
	public void setWireList(LinkedList<String> wireList) { this.wireList = wireList; }
	public LinkedList<VerilogComponent> getComponentList() { return componentList; }
	public void setComponentList(LinkedList<VerilogComponent> componentList) { this.componentList = componentList; }
	public HashMap<String, String> getAssignHash() { return assignHash; }
	public void setAssignHash(HashMap<String, String> assignHash) { this.assignHash = assignHash; }
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
		this.assignHash = new HashMap<String, String>();
		this.parsing();
	}
	
	// Methods
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
				Matcher inputMatcher = Pattern.compile("^\\s*input\\s+").matcher(line);
				if (inputMatcher.find()) {
					Matcher pinMatcher = Pattern.compile("(\\S+)\\s*,").matcher(line);
					while (pinMatcher.find()) {
						this.inputPinList.push(pinMatcher.group(1));
					}
					pinMatcher = Pattern.compile("(\\S+)\\s*;").matcher(line);
					if (pinMatcher.find()) {
						this.inputPinList.push(pinMatcher.group(1));
					}
					continue;
				}
				// Output pin match
				Matcher outputMatcher = Pattern.compile("^\\s*output\\s+").matcher(line);
				if (outputMatcher.find()) {
					Matcher pinMatcher = Pattern.compile("(\\S+)\\s*,").matcher(line);
					while (pinMatcher.find()) {
						this.outputPinList.push(pinMatcher.group(1));
					}
					pinMatcher = Pattern.compile("(\\S+)\\s*;").matcher(line);
					if (pinMatcher.find()) {
						this.outputPinList.push(pinMatcher.group(1));
					}
					continue;
				}
				// Wire match
				Matcher wireMatcher = Pattern.compile("^\\s*wire\\s+").matcher(line);
				if (wireMatcher.find()) {
					Matcher pinMatcher = Pattern.compile("(\\S+)\\s*,").matcher(line);
					while (pinMatcher.find()) {
						this.wireList.push(pinMatcher.group(1));
					}
					pinMatcher = Pattern.compile("(\\S+)\\s*;").matcher(line);
					if (pinMatcher.find()) {
						this.wireList.push(pinMatcher.group(1));
					}
					continue;
				}
				
				// Assign match
				Matcher assignMatcher = Pattern.compile("^\\s*assign\\s+(\\S+)\\s*=\\s*(\\S+)").matcher(line);
				if (assignMatcher.find()) {
					this.assignHash.put(assignMatcher.group(1), assignMatcher.group(2));
					continue;
				}

				// Component match
				Matcher componentMatcher = Pattern.compile("^\\s*(\\S+)\\s+(\\S+)\\s+\\(.*\\);").matcher(line);
				if (componentMatcher.find()) {
					VerilogComponent vc = new VerilogComponent(componentMatcher.group(2), componentMatcher.group(1));
					Matcher pinMatcher = Pattern.compile("\\.(\\S+)\\((\\S+)\\),").matcher(line);
					while (pinMatcher.find()) {
						vc.getConnectionHash().put(pinMatcher.group(1), pinMatcher.group(2));
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
