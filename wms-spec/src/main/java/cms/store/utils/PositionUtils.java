package cms.store.utils;

import cma.store.data.Bot;
import cma.store.data.Pos;



/**
Warehouse optimizer.
creating date: 2012-05-20
creating time: 23:29:22
autor: Czarek
 */

public class PositionUtils {
	
	
	// hardcoded constant unitSize = 100 -- for log purpose only
	// we should take it from LayerModel (via env)
	public static int getIntX(double x) {
		return (int) (x/100.0 - 0.5);
	}

	public static int getIntY(double y) {
		return (int) (y/100.0);
	}
	
	public static long getTimeToTrevel( Pos p1, Pos p2, Bot car){
		double speed = car.getMaxSpeed();
		double dist = Math.abs( p1.x-p2.x ) + Math.abs( p1.y-p2.y );
		return (long) (speed * dist);
	}

}
