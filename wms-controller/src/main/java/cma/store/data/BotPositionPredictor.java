package cma.store.data;

import cma.store.env.Environment;

/**
Warehouse optimizer.
creating date: 2012-07-19
creating time: 18:22:22
autor: Czarek
 */

public class BotPositionPredictor {
	Environment env;
	
	public BotPositionPredictor( Environment env ){
		this.env = env;
	}
	
	public Pos getCurrentCarPosition( Bot car ){
		return car.getPos();
	}
	
	
	public Pos getCarPosition( Bot car, long time, boolean nullWhenAfterRoute ){
		return env.getSchedule().getBotPosition(car, time, nullWhenAfterRoute);
	}
	
	
}
