package com.smb.anylogic.bot;

import java.util.LinkedList;


public class WMSBot {
	private Integer number;
	LinkedList<Task> tasks;
	
    /**
     * Default constructor
     */
    public WMSBot(){
    }
    
    public WMSBot(Integer number){
    	setNumber(number);
    }
     
	
	@Override
	public String toString() {			
		return super.toString();
	}

	/**
	 * This number is here for model snapshot storing purpose<br>
	 * It needs to be changed when this class gets changed
	 */ 
	private static final long serialVersionUID = 1L;

	
	public void setNumber(Integer number) {
		this.number = number;
	}
	
	public Integer getNumber() {
		return this.number;
	}
	
}