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
	
	public void getVisitingInfo() {
		for (LogicalGraphComponent lgcFF : this.getFFList()) {
			LinkedList<LogicalGraphComponent> trackingList = new LinkedList<LogicalGraphComponent>();
			recur_getVisitingInfo(lgcFF, lgcFF, trackingList);			
		}
	}
	
	public void recur_getVisitingInfo(LogicalGraphComponent currentLgc, LogicalGraphComponent lgcFF, LinkedList<LogicalGraphComponent> trackingList) {
		// Terminal condition
		/// 1. visited component
		if (trackingList.contains(currentLgc)) {
			return;
		}
		/// 2. visit FF or top 
		if (currentLgc != lgcFF && currentLgc.getCell().getName().matches("(.*)" + FF_NAME_PATTERN + "(.*)") || currentLgc.getCell().getName().equals("TOP")) {
			return;
		}
		// Set visit
		trackingList.add(currentLgc);
		if (currentLgc != lgcFF) {
			lgcFF.getVisitingComponentSet().add(currentLgc);
			currentLgc.getVisitingComponentSet().add(lgcFF);
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
					recur_getVisitingInfo(subLgc, lgcFF, trackingList);
					//System.err.println("*recur> Exit from: " + subLgc.getName());

				}
			}
		}
		trackingList.remove(currentLgc);
	}
	/*
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
	*/
    public static List<HashSet<LogicalGraphComponent>> sortByValue(final Map<HashSet<LogicalGraphComponent>, Double> map){
        List<HashSet<LogicalGraphComponent>> list = new ArrayList<HashSet<LogicalGraphComponent>>();
        list.addAll(map.keySet());
         
        Collections.sort(list, new Comparator<HashSet<LogicalGraphComponent>>(){
            public int compare(HashSet<LogicalGraphComponent> o1, HashSet<LogicalGraphComponent> o2){
            	Double v1 = (Double)map.get(o1);
            	Double v2 = (Double)map.get(o2);
                 
                return v1.compareTo(v2);
            }
        });
        Collections.reverse(list);
        return list;
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogicalGraph lg = new LogicalGraph(new Verilog(args[0]), new LEF(args[1]));
		lg.getVisitingInfo();
		//HashMap<FFSetElement, Integer> maximumSet = new HashMap<FFSetElement, Integer>();
		HashMap<HashSet<LogicalGraphComponent>, Double> ffHash = new HashMap<HashSet<LogicalGraphComponent>, Double>();
		// Stage1: Get possible FF combination set
		for (LogicalGraphComponent lgc : lg.getLgcList()) {
			if (lgc.getCell().getName().matches("(.*)" + LogicalGraph.FF_NAME_PATTERN + "(.*)")) { continue; }
			HashSet<LogicalGraphComponent> set = new HashSet<LogicalGraphComponent>();
			Iterator<LogicalGraphComponent> lgcIt = lgc.getVisitingComponentSet().iterator();
			while (lgcIt.hasNext()) {
				LogicalGraphComponent currentFF = lgcIt.next();
				set.add(currentFF);
			}
			if (set.size() > 0 && !ffHash.containsKey(set)) {
				ffHash.put(set, 0.0);
			}
			//System.out.println();
		}
		
		// Stage2: Calculate density
		Iterator<HashSet<LogicalGraphComponent>> ffHashIt = ffHash.keySet().iterator();
		while (ffHashIt.hasNext()) {
			//System.out.print("*");
			HashSet<LogicalGraphComponent> ffSet = ffHashIt.next();
			HashSet<LogicalGraphComponent> union = null;
			HashSet<LogicalGraphComponent> intersect = null;
			Iterator<LogicalGraphComponent> ffSetIt = ffSet.iterator();
			boolean initFlag = true;
			while (ffSetIt.hasNext()) {
				LogicalGraphComponent ffLgc = ffSetIt.next();
				if (initFlag) {
					//System.out.print(" " + ffLgc.getName());
					union = new HashSet<LogicalGraphComponent>(ffLgc.getVisitingComponentSet());
					intersect = new HashSet<LogicalGraphComponent>(ffLgc.getVisitingComponentSet());
					initFlag = false;
				} else {
					//System.out.print(", " + ffLgc.getName());
					union.addAll(ffLgc.getVisitingComponentSet());
					intersect.retainAll(ffLgc.getVisitingComponentSet());
				}
			}
			Double density = ((0.0) + intersect.size()) / union.size();
			ffHash.put(ffSet, density);
			/*
			System.out.println(" - Union Size: " + union.size() + " / Intersect Size: " + intersect.size());
			System.out.print("\tUnion:");
			Iterator<LogicalGraphComponent> unionIt = union.iterator();
			while (unionIt.hasNext()) {	System.out.print("\t" + unionIt.next().getName());	}
			System.out.println();
			System.out.print("\tIntersect:");
			Iterator<LogicalGraphComponent> intersectIt = intersect.iterator();
			while (intersectIt.hasNext()) {	System.out.print("\t" + intersectIt.next().getName());	}
			System.out.println();
			*/
		}
		
		Iterator<HashSet<LogicalGraphComponent>> itFSE = LogicalGraph.sortByValue(ffHash).iterator();
		while (itFSE.hasNext()) {
			HashSet<LogicalGraphComponent> fse = itFSE.next();
			System.out.format("[%.9f]", ffHash.get(fse));
			Iterator<LogicalGraphComponent> fseIt = fse.iterator();
			while (fseIt.hasNext()) {
				System.out.print(" " + fseIt.next().getName());
			}
			System.out.println();
		}
		/*
		for (LogicalGraphComponent ffLGC : lg.getFFList()) {
			System.out.print(ffLGC.getName());
			Iterator<LogicalGraphComponent> test = ffLGC.getVisitingComponentSet().iterator();
			while (test.hasNext()) {
				LogicalGraphComponent testLGC = test.next();
				System.out.print("\t" + testLGC.getName());
			}
			System.out.println();
		}
		*/
	}
}
