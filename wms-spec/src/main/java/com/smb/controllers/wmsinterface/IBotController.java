package com.smb.controllers.wmsinterface;

import com.smb.anylogic.bot.Task;
import com.smb.anylogic.bot.WMSBot;
import com.smb.anylogic.bot.instruction.Instruction;

public interface IBotController {
	public Instruction popNextInstruction(WMSBot bot);
	public void addTask(Integer idBot, Task task);
	public Instruction getNextInstruction(WMSBot bot);
}
