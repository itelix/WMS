package com.smb.anylogic.bot.instruction;

public class MoveToInstruction extends Instruction {
	private Point point;

	public MoveToInstruction(){
		
	}
	
	public MoveToInstruction(long timeCost) {
		setTimeCost(timeCost);
	}
	
	public Point getPoint() {
		return point;
	}

	public void setPoint(Point point) {
		this.point = point;
	}	
}
