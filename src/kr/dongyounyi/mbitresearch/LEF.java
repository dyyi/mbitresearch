package kr.dongyounyi.mbitresearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LEF {
	// Fields
	private File file;
	private HashMap<String, Cell> cellHash;

	// Getter and Setter
	public File getFile() { return file; }
	public void setFile(File file) { this.file = file; }
	public HashMap<String, Cell> getCellHash() { return cellHash; }
	public void setCellHash(HashMap<String, Cell> cellHash) { this.cellHash = cellHash; }
	
	// Constructor
	public LEF() {
		cellHash = new HashMap<String, Cell>();
	}
	public LEF(String lefFileName) {
		this(new File(lefFileName));
	}
	public LEF(File lefFile) {
		this();
		this.file = lefFile;
		parsing();
	}
	
	// Methods
	public void parsing() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(this.file));
			String line = "";
			String cellName = "";
			String pinName = "";
			Cell cell = null;
			Pin pin = null;
			while ((line = br.readLine()) != null) {
				Matcher macroMatcher = Pattern.compile("^\\s*MACRO\\s+(\\S+)").matcher(line);
				if (macroMatcher.find()) {
					cellName = macroMatcher.group(1);
					cell = new Cell(cellName);
					continue;
				}
				Matcher endMacroMatcher = Pattern.compile("^\\s*END\\s+" + cellName).matcher(line);
				if (endMacroMatcher.find() && cell != null) {
					this.cellHash.put(cell.getName(), cell);
					continue;
				}
				Matcher pinMatcher = Pattern.compile("^\\s*PIN\\s+(\\S+)").matcher(line);
				if (pinMatcher.find()) {
					pinName = pinMatcher.group(1);
					pin = new Pin();
					pin.setName(pinName);
					continue;
				}
				Matcher endPinMatcher = Pattern.compile("^\\s*END\\s+" + pinName).matcher(line);
				if (endPinMatcher.find() && cell != null && pin != null) {
					cell.getPinHash().put(pin.getName(), pin);
					continue;
				}
				Matcher directionMatcher = Pattern.compile("^\\s*DIRECTION\\s+(\\S+)").matcher(line);
				if (directionMatcher.find() && pin != null) {
					pin.setDirection(directionMatcher.group(1).toUpperCase());
					continue;
				}
				Matcher useMatcher = Pattern.compile("^\\s*USE\\s+(\\S+)").matcher(line);
				if (useMatcher.find() && pin != null) {
					pin.setUse(useMatcher.group(1).toUpperCase());
					continue;
				}
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) {
		LEF lef = new LEF(args[0]);
		for (Cell cell : lef.getCellHash().values()) {
			System.out.println("* Cell: " + cell.getName());
			for (Pin pin : cell.getPinHash().values()) {
				System.out.println("\t" + pin.getName() + " " + pin.getDirection() + " " + pin.getUse());
			}
		}
	}
}
