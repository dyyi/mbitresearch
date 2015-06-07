package kr.dongyounyi.mbitresearch;

import java.util.ArrayList;
import java.util.HashSet;

public class LogicGraphVertex {
	// Fields
	private HashSet<LogicComponent> set;
	private ArrayList<LogicGraphVertex> adjacentVertexList;
	
	public HashSet<LogicComponent> getSet() { return set; }
	public void setSet(HashSet<LogicComponent> set) { this.set = set; }
	public ArrayList<LogicGraphVertex> getAdjacentVertexList() { return adjacentVertexList; }
	public void setAdjacentVertexList(ArrayList<LogicGraphVertex> adjacentVertexList) { this.adjacentVertexList = adjacentVertexList; }

	// Constructor
	public LogicGraphVertex(HashSet<LogicComponent> set) {
		this.set = set;
		this.adjacentVertexList = new ArrayList<LogicGraphVertex>();
	}
	
	// Methods
	public Double getWeight() {
		Double sum = this.getCost();
		for (LogicGraphVertex lgv : this.adjacentVertexList) {
			sum += lgv.getCost();
		}
		sum = sum / this.getCost();
		return sum;
	}
	
	public Double getCost() {
		return 0.0 + this.set.size();
	}
	
	public void addAdjacentVertex(LogicGraphVertex lgv) {
		if (!adjacentVertexList.contains(lgv)) {
			adjacentVertexList.add(lgv);
		}
	}
	
	public void removeAdjacentVertex(LogicGraphVertex lgv) {
		if (adjacentVertexList.contains(lgv)) {
			adjacentVertexList.remove(lgv);
		}
	}
	
	public boolean isConflict(LogicGraphVertex lgv) {
		HashSet<LogicComponent> testSet = new HashSet<LogicComponent>(this.getSet());
		testSet.retainAll(lgv.getSet());
		if (testSet.size() > 0) {
			return true;
		} else {
			return false;
		}
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
