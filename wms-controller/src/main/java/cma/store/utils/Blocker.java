package cma.store.utils;

import cma.store.data.Bot;

public class Blocker {
	
	Conflict conflict;
	Bot blocker;
	
	public Blocker(Conflict conflict, Bot blocker) {
		super();
		this.conflict = conflict;
		this.blocker = blocker;
	}
	
	public String toString(){
		return ""+conflict;
	}
	
	public Conflict getConflict() {
		return conflict;
	}
	
	public Bot getBlocker() {
		return blocker;
	}

	
	
	
	

}
