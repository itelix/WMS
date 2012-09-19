package cma.store.input.request;

import cma.store.data.Pos;

/**
Warehouse optimizer.
creating date: 2012-07-17
creating time: 18:46:07
autor: Czarek
 */

public class LocPriority {
	private Pos pos;
	private double priority;
	private boolean isAssigned;
	
	public LocPriority(Pos pos, double priority) {
		super();
		this.pos = pos;
		this.priority = priority;
	}
	public Pos getPos() {
		return pos;
	}
	public double getPriority() {
		return priority;
	}
	public boolean isAssigned() {
		return isAssigned;
	}
	public void setAssigned(boolean isAssigned) {
		this.isAssigned = isAssigned;
	}
	
	public String toString() {
		return "Pos: " + pos + " priority: " + priority;
	}
}
