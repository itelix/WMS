package cma.store.data;

/**
Warehouse optimizer.
creating date: 2012-05-27
creating time: 13:45:57
autor: Czarek
 */

public class PosTime extends Pos {
	
	public long time;
	
	public PosTime(double x, double y, long time) {
		super(x, y);
		
		this.time = time;
	}
	
	public PosTime(Pos p, long time) {
		this(p.x, p.y, time);
	}

	public long getTime() {
		return time;
	}

	public String toString(){
		return ""+x+":"+y+":t="+time;
	}

}
