package cma.store.data;

/**
Warehouse optimizer.
creating date: 2012-07-24
creating time: 20:16:14
autor: Czarek
 */

public class Duration {
	private long startTime;
	private long endTime;
	
	public Duration(long startTime, long endTime) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getEndTime() {
		return endTime;
	}
	
	public long getDuration(){
		return endTime-startTime;
	}
	
	public String toString(){
		return "<"+startTime+" : "+endTime+">";
	}

}
