package com.smb.anylogic.bot.instruction;

public class DelayInstruction extends Instruction {
	private double delayTime;

	
	public DelayInstruction(double delayTime) {
		this.delayTime = delayTime;
	}
	
	public double getDelayTime() {
		return delayTime;
	}

	public void setDelayTime(double delayTime) {
		this.delayTime = delayTime;
	}

}