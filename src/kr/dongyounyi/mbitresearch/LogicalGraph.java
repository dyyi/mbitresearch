package kr.dongyounyi.mbitresearch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class LogicalGraph {
	public static String FF_NAME_PATTERN = "DFF";
	
	// Fields
	private LogicalGraphComponent top;
	private LinkedList<LogicalGraphComponent> lgcList;

	private Verilog verilog;
	private LEF lef;
	private HashMap<String, Pin> pinHash;
	private HashMap<String, Pin> inputPinHash;
	private HashMap<String, Pin> outputPinHash;
	private HashMap<String, Net> wireHash;
	private LinkedList<LogicalGraphComponent> ffList;

	// Getter and Setter
	public LogicalGraphComponent getTop() {return top;}
	public void setTop(LogicalGraphComponent top) {this.top = top;}
	public LinkedList<LogicalGraphComponent> getLgcList() {return lgcList;}
	public void setLgcList(LinkedList<LogicalGraphComponent> ffList) {this.lgcList = lgcList;}
	public Verilog getVerilog() {return verilog;}
	public void setVerilog(Verilog verilog) {this.verilog = verilog;}
	public LEF getLef() {return lef;}
	public void setLef(LEF lef) {this.lef = lef;}
	public HashMap<String, Pin> getPinHash() {return pinHash;}
	public void setPinHash(HashMap<String, Pin> pinHash) {this.pinHash = pinHash;}
	public HashMap<String, Pin> getInputPinHash() {return inputPinHash;}
	public void setInputPinHash(HashMap<String, Pin> inputPinHash) {this.inputPinHash = inputPinHash;}
	public HashMap<String, Pin> getOutputPinHash() {return outputPinHash;}
	public void setOutputPinHash(HashMap<String, Pin> outputPinHash) {this.outputPinHash = outputPinHash;}
	public HashMap<String, Net> getWireHash() {return wireHash;}
	public void setWireHash(HashMap<String, Net> wireHash) {this.wireHash = wireHash;}
	public LinkedList<LogicalGraphComponent> getFFList() {return ffList;}
	public void setFFList(LinkedList<LogicalGraphComponent> ffList) {this.ffList = ffList;}

	// Constructor
	public LogicalGraph(Verilog verilog, LEF lef) {
		this.top = new LogicalGraphComponent();
		this.top.setName("TOP");
		this.top.setCell(new Cell("TOP"));
		this.lgcList = new LinkedList<LogicalGraphComponent>();
		this.verilog = verilog;
		this.lef = lef;
		pinHash = new HashMap<String, Pin>();
		inputPinHash = new HashMap<String, Pin>();
		outputPinHash = new HashMap<String, Pin>();
		wireHash = new HashMap<String, Net>();
		ffList = new LinkedList<LogicalGraphComponent>();
		buildGraphFromVerilog();
	}
	
	// Methods
	private void buildGraphFromVerilog() {
		for (String pinName : verilog.getInputPinList()) {
			inputPinHash.put(pinName, new Pin(pinName));
		}
		for (String pinName : verilog.getOutputPinList()) {
			outputPinHash.put(pinName, new Pin(pinName));
		}
		pinHash.putAll(inputPinHash);
		pinHash.putAll(outputPinHash);
		for (String wireName : verilog.getWireList()) {
			Net net = new Net(wireName);
			wireHash.put(wireName, net);
			if (pinHash.containsKey(wireName)) {
				if (net.getConnectionHash().containsKey(this.top) == false) {
					LinkedList<Pin> pinList = new LinkedList<Pin>();
					pinList.add(pinHash.get(wireName));
					net.getConnectionHash().put(this.top, pinList);
				} else {
					net.getConnectionHash().get(this.top).add(pinHash.get(wireName));
				}
			}
		}
					
		Iterator<String> assignIt = verilog.getAssignHash().keySet().iterator();
		while (assignIt.hasNext()) {
			String pinName = assignIt.next();
			String wireName = verilog.getAssignHash().get(pinName);
			Pin pin = null;
			if (inputPinHash.containsKey(pinName)) {
				pin = inputPinHash.get(pinName);
			} else if (outputPinHash.containsKey(pinName)) {
				pin = outputPinHash.get(pinName);
			} else {
				pin = new Pin(pinName);
			}
			Net net = null;
			if (wireHash.containsKey(wireName)) {
				net = wireHash.get(wireName);
			} else {
				net = new Net(wireName);
			}
			this.top.getConnectionHash().put(pin, net);
			if (net.getConnectionHash().containsKey(this.top) == false) {
				LinkedList<Pin> pinList = new LinkedList<Pin>();
				pinList.add(pin);
				net.getConnectionHash().put(this.top, pinList);
			} else {
				net.getConnectionHash().get(this.top).add(pin);
			}
		}
		
		// Read verilog components
		for (VerilogComponent component : verilog.getComponentList()) {
			LogicalGraphComponent lgc = new LogicalGraphComponent();
			lgc.setName(component.getInstanceName());
			lgc.setCell(lef.getCellHash().get(component.getCellName()));
			if (lgc.getCell() == null) {
				System.err.println("## ERROR ## Can't find cell " + component.getCellName() + " from LEF file!!");
			}
			Iterator<String> it = component.getConnectionHash().keySet().iterator();
			while (it.hasNext()) {
				String pinName = it.next();
				Pin pin = lgc.getCell().getPinHash().get(pinName);
				if (pin == null) {
					System.err.println("## ERROR ## Can't find pin " + pinName + " from cell " + lgc.getCell().getName() + " !!!");
				}
				String netName = component.getConnectionHash().get(pinName);
				Net net = null;
				if (wireHash.containsKey(netName)) {
					net = wireHash.get(netName);
				} else {
					net = new Net(netName);
					wireHash.put(netName, net);
					if (pinHash.containsKey(netName)) {
						if (net.getConnectionHash().containsKey(this.top) == false) {
							LinkedList<Pin> pinList = new LinkedList<Pin>();
							pinList.add(pinHash.get(netName));
							net.getConnectionHash().put(this.top, pinList);
						} else {
							net.getConnectionHash().get(this.top).add(pinHash.get(netName));
						}
					}
				}
				// Connect pin to net
				if (net.getConnectionHash().containsKey(lgc) == false) {
					LinkedList<Pin> pinList = new LinkedList<Pin>();
					pinList.add(pin);
					net.getConnectionHash().put(lgc, pinList);
				} else {
					net.getConnectionHash().get(lgc).add(pin);
				}
				// Connect net to pin
				lgc.getConnectionHash().put(pin,  net);
			}
			
			if (lgc.getCell().getName().matches("(.*)" + FF_NAME_PATTERN + "(.*)")) {
				ffList.add(lgc);
			}
			
			if (!this.lgcList.contains(lgc)) {
				lgcList.add(lgc);
			}
		}
	}
	
	public void calculateFFDelta() {
		for (LogicalGraphComponent lgcFF : this.getFFList()) {
			lgcFF.setVisit(true);
			lgcFF.setDescendentCount(0);
			LinkedList<LogicalGraphComponent> trackingList = new LinkedList<LogicalGraphComponent>();
			trackingList.add(lgcFF);
			Iterator<Pin> it = lgcFF.getConnectionHash().keySet().iterator();
			//System.err.println("* Start from " + lgcFF.getName());
			while (it.hasNext()) {
				Pin pin = it.next();
				if (pin.getDirection().equals("OUTPUT")) {
					Net net = lgcFF.getConnectionHash().get(pin);
					//System.err.println("* through pin: " + pin.getName() + " and net: " + net.getName());
					Iterator<LogicalGraphComponent> subIt = net.getConnectionHash().keySet().iterator();
					while (subIt.hasNext()) {
						LogicalGraphComponent subLgc = subIt.next();
						boolean inputCheck = false;
						for (Pin tempPin : net.getConnectionHash().get(subLgc)) {
							if (tempPin.getDirection().equals("INPUT")) {
								inputCheck = true;
								//System.err.println("* To instance: " + subLgc.getName() + " / pin: " + tempPin.getName());
							}
						}
						if (!inputCheck) { continue; }
						lgcFF.addDescendentCount(calculateFFDelta_recur(subLgc, lgcFF, lgcFF, trackingList));
						trackingList.remove(subLgc);
						//System.err.println("* Exit from: " + subLgc.getName());
					}
				}
			}
		}
	}
	private int calculateFFDelta_recur(LogicalGraphComponent currentLgc, LogicalGraphComponent lgcFF, LogicalGraphComponent parentLgc, LinkedList<LogicalGraphComponent> trackingList) {
		// If FF, return 1
		if (currentLgc.getCell().getName().matches("(.*)" + FF_NAME_PATTERN + "(.*)") || currentLgc.getCell().getName().equals("TOP")) {
			//System.err.println("** END");
			return 0;
		}
		if (currentLgc.isVisit()) {
			if (trackingList.contains(currentLgc)) {
				// Feedback loop !!!
				System.err.println("* Feedback loop detected at " + currentLgc.getName() + " trace from " + lgcFF.getName());
				for (LogicalGraphComponent tempLgc : trackingList) {
					//System.err.print("  - " + tempLgc.getName());
					Iterator<Pin> debugPinIt = tempLgc.getConnectionHash().keySet().iterator();
					while (debugPinIt.hasNext()) {
						Pin debugPin = debugPinIt.next();
						//System.err.print(" " + debugPin.getName() + "(" + tempLgc.getConnectionHash().get(debugPin).getName() + ")");
					}
					//System.err.println();
				}
				return 0;
			} else {
				// Return value and add visiting FF
				if (!currentLgc.getVisitingFFList().contains(lgcFF)) {
					currentLgc.getVisitingFFList().add(lgcFF);
				}
				lgcFF.getVisitingComponentSet().add(currentLgc);
				parentLgc.getVisitingComponentSet().add(currentLgc);
				parentLgc.getVisitingComponentSet().addAll(currentLgc.getVisitingComponentSet());
				return currentLgc.getDescendentCount() + 1;
			}
		} else {
			lgcFF.getVisitingComponentSet().add(currentLgc);
			parentLgc.getVisitingComponentSet().add(currentLgc);
			trackingList.add(currentLgc);
			currentLgc.setVisit(true);
			currentLgc.setDescendentCount(0);
			if (!currentLgc.getVisitingFFList().contains(lgcFF)) {
				currentLgc.getVisitingFFList().add(lgcFF);
			}
			Iterator<Pin> it = currentLgc.getConnectionHash().keySet().iterator();
			while (it.hasNext()) {
				Pin pin = it.next();
				if (pin.getDirection().equals("OUTPUT")) {
					Net net = currentLgc.getConnectionHash().get(pin);
					//System.err.println("*recur> through pin: " + pin.getName() + " and net: " + net.getName());
					Iterator<LogicalGraphComponent> subIt = net.getConnectionHash().keySet().iterator();
					while (subIt.hasNext()) {
						LogicalGraphComponent subLgc = subIt.next();
						boolean inputCheck = false;
						for (Pin tempPin : net.getConnectionHash().get(subLgc)) {
							if (tempPin.getDirection().equals("INPUT")) {
								inputCheck = true;
								//System.err.println("*recur> To instance: " + subLgc.getName() + " / pin: " + tempPin.getName());
							}
						}
						if (!inputCheck) { continue; }
						currentLgc.addDescendentCount(calculateFFDelta_recur(subLgc, lgcFF, currentLgc, trackingList));
						trackingList.remove(subLgc);
						//System.err.println("*recur> Exit from: " + subLgc.getName());

					}
				}
			}
			parentLgc.getVisitingComponentSet().addAll(currentLgc.getVisitingComponentSet());
			return currentLgc.getDescendentCount() + 1;
		}
	}
    public static List sortByValue(final Map map){
        List<HashSet> list = new ArrayList<HashSet>();
        list.addAll(map.keySet());
         
        Collections.sort(list,new Comparator(){
             
            public int compare(Object o1,Object o2){
                Object v1 = map.get(o1);
                Object v2 = map.get(o2);
                 
                return ((Comparable) v1).compareTo(v2);
            }
             
        });
        Collections.reverse(list);
        return list;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogicalGraph lg = new LogicalGraph(new Verilog(args[0]), new LEF(args[1]));
		lg.calculateFFDelta();
		//HashMap<FFSetElement, Integer> maximumSet = new HashMap<FFSetElement, Integer>();
		HashMap<HashSet, Double> maximumSet = new HashMap<HashSet, Double>();
		for (LogicalGraphComponent lgc : lg.getLgcList()) {
			if (lgc.getCell().getName().matches("(.*)" + LogicalGraph.FF_NAME_PATTERN + "(.*)")) { continue; }
			if (lgc.getDescendentCount() == null) { continue; }
			//FFSetElement fse = new FFSetElement(lgc.getVisitingFFList(), lgc.getDescendentCount());
			HashSet<String> set = new HashSet<String>();
			Double delta = 0.0;
			Double ffDescendent = 0.0;
			HashSet<LogicalGraphComponent> union = new HashSet<LogicalGraphComponent>();
			for (LogicalGraphComponent ff : lgc.getVisitingFFList()) {
				set.add(ff.getName());
				union.addAll(ff.getVisitingComponentSet());
				ffDescendent += ff.getDescendentCount();
			}
			ffDescendent = 0.0 + union.size();
			delta = lgc.getDescendentCount() / ffDescendent;
			if (maximumSet.containsKey(set)) {
				if (maximumSet.get(set) < delta) {
					maximumSet.put(set, delta);
				}
			} else {
				maximumSet.put(set, delta);
			}
			System.out.print("- " + lgc.getName() + " [" + delta + "=" + lgc.getDescendentCount() + "/" + ffDescendent + "] - ");
			for (LogicalGraphComponent fflgc : lgc.getVisitingFFList()) {
				System.out.print(" " + fflgc.getName());
			}
			System.out.println();
		}
		
		System.out.println("------------------------------------------");
		
		Iterator<HashSet> itFSE = LogicalGraph.sortByValue(maximumSet).iterator();
		while (itFSE.hasNext()) {
			HashSet<String> fse = itFSE.next();
			System.out.format("[%.9f]", maximumSet.get(fse));
			Iterator<String> fseIt = fse.iterator();
			while (fseIt.hasNext()) {
				System.out.print(" " + fseIt.next());
			}
			System.out.println();
		}
		
		for (LogicalGraphComponent ffLGC : lg.getFFList()) {
			System.out.print(ffLGC.getName());
			Iterator<LogicalGraphComponent> test = ffLGC.getVisitingComponentSet().iterator();
			while (test.hasNext()) {
				LogicalGraphComponent testLGC = test.next();
				System.out.print("\t" + testLGC.getName());
			}
			System.out.println();
		}
		
	}
}
