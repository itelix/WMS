package cma.store.data;

/**
Warehouse optimizer.
creating date: 25-07-2012
creating time: 14:29:40
autor: Filip
 */

public class PosAStar extends Pos {
	private double dist;
	
	public PosAStar(double x, double y, double dist) {
		super(x, y);
		this.dist = dist;
	}
	
	public PosAStar(Pos pos, double dist) {
		super(pos.x, pos.y);
		this.dist = dist;
	}

	public double getWeight() {
		return dist;
	}
	
	public void setWeight(double dist) {
		this.dist = dist;
	}
	
}
