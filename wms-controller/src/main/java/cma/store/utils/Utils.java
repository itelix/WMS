package cma.store.utils;

import cma.store.control.opt.route.matrix.MatrixScore;
import cma.store.control.opt.route.matrix.RouteScorring;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.PosTime;
import cma.store.input.request.LocPriority;

/**
Warehouse optimizer.
creating date: 2012-05-20
creating time: 23:29:22
autor: Czarek
 */

public class Utils {
	
//	public static final boolean isColision( Environment env, Bot b1, Bot b2, Pos p1, Pos p2 ){
//		
//		if( b1==b2 ) return false;
//		
//		//TODO should be bot dependent and much more complicated (speed, roads structure, ...)
//		return env.getSaveDistance() >= distance(p1,p2);
//	}
	
	public static void printLayer( RouteScorring routScorring, int time ){
		
		int maxX = routScorring.getMaxXdisc();
		int maxY = routScorring.getMaxXdisc();
		
		for(int i=0; i<maxY; i++){
			for(int j=0; j<maxX; j++){
				PosTime p = new PosTime(i,j,time);
			
				MatrixScore ms= routScorring.getMatrixScoreLayer(p);
				
				if( ms==null ){
					System.out.print( ", " );
				
				}else{
					System.out.print( "," + (int)ms.cost );
					
				}
			}
			System.out.println("");
		}
	}
	
	public static double distance( Pos p1, Pos p2 ){
		return Math.abs(p1.x-p2.x) + Math.abs(p1.y-p2.y);
	}
	
	/**
	 * Count distance from location to bot
	 * @param lp
	 * @param car
	 * @return distance
	 */
	public static double distance( LocPriority lp, Bot car ){
		Pos p = lp.getPos();
		return Math.abs(p.x-car.getX()) + Math.abs(p.y-car.getY());

	}
	
	public static long minTimeToTrevel( Bot bot, Pos pos1, Pos pos2 ){
		//TODO calculation is to raw
		double dist = Math.abs(pos1.x-pos2.x) + Math.abs(pos1.y-pos2.y);
		
		return (long)(dist / bot.getMaxSpeed());
	}


}
