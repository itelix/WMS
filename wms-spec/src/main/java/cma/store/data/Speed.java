package cma.store.data;

/**
Warehouse optimizer.
creating date: 2012-07-24
creating time: 20:08:26
autor: Czarek
 */

public class Speed {
	
	private double x;
	private double y;
	
	/**
	 * 
	 * @param x speed in mm per milisecond
	 * @param y speed in mm per milisecond
	 */	
	public Speed(double x, double y) {
		super();
		this.x = x;
		this.y = y;
	}

	public String toString(){
		return ""+x+" : "+y;
	}	
	
	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}
	

	

}
