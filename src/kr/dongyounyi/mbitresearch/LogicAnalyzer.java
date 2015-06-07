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

public class LogicAnalyzer {
	public static String FF_NAME_PATTERN = "DFF";
	
	// Fields
	private LogicComponent top;
	private LinkedList<LogicComponent> lgcList;

	private Verilog verilog;
	private LEF lef;
	private HashMap<String, Pin> pinHash;
	private HashMap<String, Pin> inputPinHash;
	private HashMap<String, Pin> outputPinHash;
	private HashMap<String, Net> wireHash;
	private LinkedList<LogicComponent> ffList;

	// Getter and Setter
	public LogicComponent getTop() {return top;}
	public void setTop(LogicComponent top) {this.top = top;}
	public LinkedList<LogicComponent> getLgcList() {return lgcList;}
	public void setLgcList(LinkedList<LogicComponent> ffList) {this.lgcList = lgcList;}
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
	public LinkedList<LogicComponent> getFFList() {return ffList;}
	public void setFFList(LinkedList<LogicComponent> ffList) {this.ffList = ffList;}

	// Constructor
	public LogicAnalyzer(Verilog verilog, LEF lef) {
		this.top = new LogicComponent();
		this.top.setName("TOP");
		this.top.setCell(new Cell("TOP"));
		this.lgcList = new LinkedList<LogicComponent>();
		this.verilog = verilog;
		this.lef = lef;
		pinHash = new HashMap<String, Pin>();
		inputPinHash = new HashMap<String, Pin>();
		outputPinHash = new HashMap<String, Pin>();
		wireHash = new HashMap<String, Net>();
		ffList = new LinkedList<LogicComponent>();
		buildGraphFromVerilog();
		getVisitingInfo();

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
			LogicComponent lgc = new LogicComponent();
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
	
	private void getVisitingInfo() {
		for (LogicComponent lgcFF : this.getFFList()) {
			LinkedList<LogicComponent> trackingList = new LinkedList<LogicComponent>();
			recur_getVisitingInfo(lgcFF, lgcFF, trackingList);			
		}
	}
	
	private void recur_getVisitingInfo(LogicComponent currentLgc, LogicComponent lgcFF, LinkedList<LogicComponent> trackingList) {
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
				Iterator<LogicComponent> subIt = net.getConnectionHash().keySet().iterator();
				while (subIt.hasNext()) {
					LogicComponent subLgc = subIt.next();
					boolean inputCheck = false;
					for (Pin tempPin : net.getConnectionHash().get(subLgc)) {
						if (tempPin == null || tempPin.getDirection() == null) {
							continue;
						}
						if (tempPin.getDirection().equals("INPUT")) {
							inputCheck = true;
						}
					}
					if (!inputCheck) { continue; }
					recur_getVisitingInfo(subLgc, lgcFF, trackingList);

				}
			}
		}
		trackingList.remove(currentLgc);
	}

	public HashMap<HashSet<LogicComponent>, Double> getDensityHash() {
		HashMap<HashSet<LogicComponent>, Double> ffHash = new HashMap<HashSet<LogicComponent>, Double>();
		// Stage1: Get possible FF combination set
		for (LogicComponent lgc : this.getLgcList()) {
			HashSet<LogicComponent> set = new HashSet<LogicComponent>();
			if (lgc.getCell().getName().matches("(.*)" + LogicAnalyzer.FF_NAME_PATTERN + "(.*)")) {
				set.add(lgc);
			} else {
				Iterator<LogicComponent> lgcIt = lgc.getVisitingComponentSet().iterator();
				while (lgcIt.hasNext()) {
					LogicComponent currentFF = lgcIt.next();
					set.add(currentFF);
				}
			}
			if (set.size() > 0 && !ffHash.containsKey(set)) {
				ffHash.put(set, 0.0);
			}
		}
		
		// Stage2: Calculate density
		Iterator<HashSet<LogicComponent>> ffHashIt = ffHash.keySet().iterator();
		while (ffHashIt.hasNext()) {
			HashSet<LogicComponent> ffSet = ffHashIt.next();
			HashSet<LogicComponent> union = null;
			HashSet<LogicComponent> intersect = null;
			Iterator<LogicComponent> ffSetIt = ffSet.iterator();
			boolean initFlag = true;
			while (ffSetIt.hasNext()) {
				LogicComponent ffLgc = ffSetIt.next();
				if (initFlag) {
					union = new HashSet<LogicComponent>(ffLgc.getVisitingComponentSet());
					intersect = new HashSet<LogicComponent>(ffLgc.getVisitingComponentSet());
					initFlag = false;
				} else {
					union.addAll(ffLgc.getVisitingComponentSet());
					intersect.retainAll(ffLgc.getVisitingComponentSet());
				}
			}
			Double density = ((0.0) + intersect.size()) / union.size();
			ffHash.put(ffSet, density);
		}
		
		return ffHash;
	}
	
	public void printMergeFFList(Double limit) {
		HashMap<HashSet<LogicComponent>, Double> ffHash = this.getDensityHash();
		Iterator<HashSet<LogicComponent>> itFSE = LogicAnalyzer.sortByValue(ffHash).iterator();
		ArrayList<LogicGraphVertex> arrList = new ArrayList<LogicGraphVertex>();
		while (itFSE.hasNext()) {
			HashSet<LogicComponent> fse = itFSE.next();
			if (ffHash.get(fse) < limit) {
				break;
			}
			LogicGraphVertex lgv = new LogicGraphVertex(fse);
			//System.out.println(ffHash.get(fse));
			if (!arrList.contains(lgv)) {
				arrList.add(lgv);
			}
		}
		
		for (LogicGraphVertex lgv : arrList) {
			for (LogicGraphVertex secondLgv : arrList) {
				if (lgv == secondLgv) {
					continue;
				}
				if (lgv.isConflict(secondLgv)) {
					lgv.addAdjacentVertex(secondLgv);
					secondLgv.addAdjacentVertex(lgv);
				}
			}
		}
		LinkedList<LogicComponent> resultCheckList = new LinkedList<LogicComponent>();
		while (arrList.size() > 0) {
			LogicGraphVertex maxLgv = null;
			Double maxWeight = 0.0;
			for (LogicGraphVertex lgv : arrList) {
				if (maxLgv == null || maxWeight > lgv.getWeight()) {
					maxLgv = lgv;
					maxWeight = lgv.getWeight();
				}
			}
			// Print Set
			if (maxLgv.getSet().size() > 1) {
				System.out.print("create_multibit {");
				Iterator<LogicComponent> maxLgvSetIt = maxLgv.getSet().iterator();
				while (maxLgvSetIt.hasNext()) {
					LogicComponent lc = maxLgvSetIt.next();
					System.out.print(" " + lc.getName());
					if (resultCheckList.contains(lc)) {
						System.err.println("Conflict! " + lc.getName());
						System.exit(0);
					}
					resultCheckList.add(lc);
				}
				System.out.println(" }");
			}
			
			// Remove Node
			while (maxLgv.getAdjacentVertexList().size() > 0) {
				LogicGraphVertex adjacentLgv = maxLgv.getAdjacentVertexList().get(0);
				for (LogicGraphVertex farAdjLgv : adjacentLgv.getAdjacentVertexList()) {
					farAdjLgv.removeAdjacentVertex(adjacentLgv);
				}
				arrList.remove(adjacentLgv);
				maxLgv.getAdjacentVertexList().remove(adjacentLgv);
			}
			arrList.remove(maxLgv);
		}
	}
	
    public static List<HashSet<LogicComponent>> sortByValue(final Map<HashSet<LogicComponent>, Double> map){
        List<HashSet<LogicComponent>> list = new ArrayList<HashSet<LogicComponent>>();
        list.addAll(map.keySet());
         
        Collections.sort(list, new Comparator<HashSet<LogicComponent>>(){
            public int compare(HashSet<LogicComponent> o1, HashSet<LogicComponent> o2){
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
		LogicAnalyzer lg = new LogicAnalyzer(new Verilog(args[0]), new LEF(args[1]));
		lg.printMergeFFList(Double.parseDouble(args[2]));
	}
}
