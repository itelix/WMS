package com.smb.anylogic.bot.instruction;

import com.smb.anylogic.bot.Task;
/**
 * Class represent specyfic instruction for bot like move or rotate
 * @author adam
 *
 */
public abstract class Instruction {

	/**
	 * Task for instruction
	 */
	Task task;
	/**
	 * Time needed to make this task
	 */
	long timeCost;

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public long getTimeCost() {
		return timeCost;
	}

	public void setTimeCost(long timeCost) {
		this.timeCost = timeCost;
	}
}