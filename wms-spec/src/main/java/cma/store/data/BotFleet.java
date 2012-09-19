package cma.store.data;

import java.util.ArrayList;
import java.util.List;


/**
Warehouse optimizer.
creating date: 2012-05-14
creating time: 00:18:23
autor: Czarek
 */

public class BotFleet {
	
	private List<Bot> bots;

	public BotFleet(){
		bots = new ArrayList<Bot>();
	}
	
	public void addBot( Bot c ){
		bots.add( c );
	}
	
	public void addBots( List<Bot> botList ){
		bots.addAll( botList );
	}
	
	public List<Bot> getBots(){
		return bots;
	}


}
