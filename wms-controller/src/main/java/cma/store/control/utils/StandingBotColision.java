package cma.store.control.utils;

import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Pos;

public class StandingBotColision extends Colision {

	long conflictedBotEndTime;
	Bot conflictedBot;
	
	public StandingBotColision(long time, Pos pos, List<Bot> colidingBots) {
		super(time, pos, colidingBots);
	}
	
	public StandingBotColision(long time, Pos pos, List<Bot> colidingBots,long conflictedBotEndTime,Bot conflictedBot) {
		super(time, pos, colidingBots);
		this.conflictedBotEndTime = conflictedBotEndTime;
		this.conflictedBot = conflictedBot;
	}

	public Bot getConflictedBot() {
		return conflictedBot;
	}
	
}