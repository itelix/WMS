package cma.store.control.opt;

import cma.store.data.Bot;
import cma.store.data.Pos;

/**
Warehouse optimizer.
creating date: 2012-07-18
creating time: 21:11:22
autor: Czarek
 */

public class BotProdLocation {
	private Bot bot;
	private Pos productPos;
	private Pos botStartPos;
	
	public BotProdLocation(Bot bot, Pos productPos, Pos botStartPos ) {
		super();
		this.bot = bot;
		this.productPos = productPos;
		this.botStartPos = botStartPos;
	}

	public Bot getBot() {
		return bot;
	}

	public Pos getProdPos() {
		return productPos;
	}

	public Pos getBotStartPos() {
		return botStartPos;
	}

	public void setBotStartPos(Pos botStartPos) {
		this.botStartPos = botStartPos;
	}
	
	
	
	

}
