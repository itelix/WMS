package cma.store.control;

import cma.store.data.Pos;
import cma.store.data.PosTime;
import cma.store.env.Environment;
import cma.store.schedule.ScheduleItem;

/**
Warehouse optimizer.
creating date: 2012-07-15
creating time: 18:54:11
autor: Czarek
 */

public class PenaltyInfo implements PenaltyInfoIfc {
	public static final double PENALTY_FORBIDDEN_WAY   = 1000;

	static final double PENALTY_NO_ROAD 		  = 1000000;
	static final double PENALTY_COLISON   	  	  = 10000;
	static final double PENALTY_CHANGE_DIRECTION  = 2;
	static final double PENALTY_TIME  		  	  = 1;
	
	
	//private Schedule schedule;
	//private LayerModel model;
	
	private Environment env;
	
	public PenaltyInfo( Environment env ){
		this.env = env;
	}


	@Override
	public double getPenalty(double x, double y, long time ) {
		if( ! env.getLayerModel().isRoad(x, y) ){
			return PENALTY_NO_ROAD;
		}
		
		for( ScheduleItem item:  env.getSchedule().getItems() ){
			long timeAcc = 100;
			double spaceAcc = 1;
//			
//			if( time < item.getRoute().getStartTime()-timeAcc ) continue; //don't overlaps
//			if( time > item.getRoute().getEndTime()+timeAcc ) continue; //don't overlaps
			
			for(long t=time-timeAcc; t<=time-timeAcc; t+=timeAcc ){
				Pos p = item.getRoute().getPos(time-timeAcc, true);
				if( p==null ) continue;
				if( x>=p.x-spaceAcc && x<=p.x+spaceAcc ){
					if( y>=p.y-spaceAcc && y<=p.y+spaceAcc ){
						return PENALTY_COLISON;
					}	
				}
			}
		}
		
		return PENALTY_TIME;
	}

	@Override
	public double getPenalty(PosTime pt) {
		return getPenalty(pt.x, pt.y, pt.time);
	}

}
