package cma.store.utils;

import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;

public class Conflict {
	
	public Bot b1;
	public Bot b2;
	public Pos p1;
	public Pos p2;
	public Route r1;
	public Route r2;
	public long time;
	private String reason;
	
	public Conflict( String reason ){
		this.reason = reason;
	}
	
	public Conflict(Bot b1, Bot b2, Pos p1, Pos p2, Route r1, Route r2,
			long time) {
		super();
		this.b1 = b1;
		this.b2 = b2;
		this.p1 = p1;
		this.p2 = p2;
		this.r1 = r1;
		this.r2 = r2;
		this.time = time;
	}


	@Override
	public String toString() {
		if( reason==null ) this.reason = "";
		
		return "Conflict " + reason+ " [b1=" + b1 + ", b2=" + b2 + ", p1=" + p1 + ", p2="
				+ p2 + ", time=" + time + ", r1=" + r1 + ", r2=" + r2  + "]";
	}
	
	
	
	
	
	

}
