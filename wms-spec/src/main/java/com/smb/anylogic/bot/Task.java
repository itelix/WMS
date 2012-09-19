package com.smb.anylogic.bot;

import java.util.LinkedList;

import com.smb.anylogic.bot.instruction.Instruction;

/**
 * This class represent task (transaction) for the bot
 * @author adam
 *
 */

public class Task {
	/**
	 * List of instruction
	 */
	LinkedList<Instruction> instructions;
	int priority;
	double taskId;
	
	public void getTest() {
		
	}

    /**
     * Default constructor
     */
    public Task(){
    	this.instructions = new LinkedList<Instruction>();
    }
    
    
	
	public LinkedList<Instruction> getInstructions() {
		return instructions;
	}



	public void setInstructions(LinkedList<Instruction> instructions) {
		this.instructions = instructions;
	}



	public int getPriority() {
		return priority;
	}



	public void setPriority(int priority) {
		this.priority = priority;
	}



	public double getTaskId() {
		return taskId;
	}



	public void setTaskId(double taskId) {
		this.taskId = taskId;
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
}
