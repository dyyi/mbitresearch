package kr.dongyounyi.mbitresearch;

import java.util.LinkedList;

public class FFSetElement {
	private LinkedList<String> ffList;
	private Integer delta;
	private Integer joinArea;
	private Integer intersectArea;
	
	public LinkedList<String> getFFList() { return ffList; }
	public void setFFList(LinkedList<String> ffList) { this.ffList = ffList; }
	public Integer getDelta() { return delta; }
	public void setDelta(Integer delta) { this.delta = delta; }

	public FFSetElement(LinkedList<LogicalGraphComponent> ffLgcList, Integer intersectWeight) {
		this.intersectArea = intersectWeight;
		this.joinArea = 0;
		ffList = new LinkedList<String>();
		for (LogicalGraphComponent lgc : ffLgcList) {
			ffList.add(lgc.getName());
			this.joinArea += lgc.getDescendentCount();
		}
		this.delta = this.intersectArea / this.joinArea;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null || !obj.getClass().equals(this.getClass()))
			return false;
		for (String thisName : this.getFFList()) {
			boolean rst = false;
			for (String objName : ((FFSetElement)obj).getFFList()) {
				if (thisName.equals(objName)) { rst = true; }
			}
			if (!rst) { return false; }
		}
		for (String objName : ((FFSetElement)obj).getFFList()) {
			boolean rst = false;
			for (String thisName : this.getFFList()) {
				if (objName.equals(objName)) { rst = true; }
			}
			if (!rst) { return false; }
		}
		return true;
	}
}
