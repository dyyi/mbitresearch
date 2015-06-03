package kr.dongyounyi.mbitresearch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class LogicalGraph {
	public final String FF_NAME_PATTERN = "DFF";
	
	// Fields
	private Verilog verilog;
	private LEF lef;
	private HashMap<String, Pin> inputPinHash;
	private HashMap<String, Pin> outputPinHash;
	private HashMap<String, Net> wireHash;
	private LinkedList<LogicalGraphComponent> ffList;

	
	public LogicalGraph(Verilog verilog, LEF lef) {
		this.verilog = verilog;
		this.lef = lef;
		inputPinHash = new HashMap<String, Pin>();
		outputPinHash = new HashMap<String, Pin>();
		wireHash = new HashMap<String, Net>();
		ffList = new LinkedList<LogicalGraphComponent>();
		buildGraphFromVerilog();
	}
	
	private void buildGraphFromVerilog() {
		for (String pinName : verilog.getInputPinList()) {
			inputPinHash.put(pinName, new Pin(pinName));
		}
		for (String pinName : verilog.getOutputPinList()) {
			outputPinHash.put(pinName, new Pin(pinName));
		}
		for (String wireName : verilog.getWireList()) {
			wireHash.put(wireName, new Net(wireName));
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
				}
				if (net.getConnectionHash().containsKey(lgc) == false) {
					LinkedList<Pin> pinList = new LinkedList<Pin>();
					pinList.add(pin);
					net.getConnectionHash().put(lgc, pinList);
				} else {
					net.getConnectionHash().get(lgc).add(pin);
				}
				lgc.getConnectionHash().put(pin,  net);
			}
			
			if (lgc.getCell().getName().matches("(.*)" + FF_NAME_PATTERN + "(.*)")) {
				ffList.add(lgc);
				System.out.println("* Adding ff " + lgc.getName());
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LogicalGraph lgc = new LogicalGraph(new Verilog(args[0]), new LEF(args[1]));
	}
}
