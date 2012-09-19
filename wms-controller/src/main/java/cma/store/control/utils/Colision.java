package cma.store.control.utils;

import java.util.List;

import cma.store.data.Bot;
import cma.store.data.Pos;

/**
Warehouse optimizer.
creating date: 2012-07-26
creating time: 20:36:17
autor: Czarek
 */

public class Colision {
	private long time;
	private Pos pos;
	private List<Bot> colidingBots;

	public Colision(long time, Pos pos, List<Bot> colidingBots) {
		this.setTime(time);
		this.setPos(pos);
		this.setColidingBots(colidingBots);
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public Pos getPos() {
		return pos;
	}

	public void setPos(Pos pos) {
		this.pos = pos;
	}

	public List<Bot> getColidingBots() {
		return colidingBots;
	}

	public void setColidingBots(List<Bot> colidingBots) {
		this.colidingBots = colidingBots;
	}
	
	public String toString() {
		String s = "Collision(time: " + time + ", pos: " + pos + ", colidingBots:";
		if(colidingBots != null) {
			for (Bot b : colidingBots)
				s += " " + b.getId();
			s += ")";
		} else {
			s += "colidingBots is null";
		}
		return s;
	}
}
