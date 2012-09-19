package cma.store.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.config.SettingProperties;
import cma.store.control.Controller;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.env.Environment;
import cma.store.schedule.Schedule;
import cma.store.schedule.ScheduleItem;

public class ConflictFinder {

	static Logger log = Logger.getLogger(ConflictFinder.class.getClass());
	
	public static Conflict getConflict( long time, Schedule schedule, Environment env ){

		List<Bot> bots = env.getBotFleet().getBots();
		for( Bot b1: bots){
			Pos p1 = schedule.getBotPosition(b1, time, false);
			for( Bot b2: bots){
				if( b2==b1) continue;
				Pos p2 = schedule.getBotPosition(b2, time, false);
				
				double d1 = Math.abs(p1.x-p2.x);
				double d2 = Math.abs(p1.y-p2.y);
				double safeDist = env.getSaveDistance();
				boolean useRealisticTimes = Boolean.parseBoolean((String) SettingProperties
		                .getInstance().getValue("external.properties",
		                        Controller.REALISTIC_TIMES)); 
				if (useRealisticTimes) {
					// getSaveDistance is to big. We need distance in timespace not in space only.
					safeDist = 0;
				}
				if( d1<=safeDist && d2<=safeDist ) {
					Route r1 = schedule.getLastRoute(b1,time);
					Route r2 = schedule.getLastRoute(b2,time);
					
					return new Conflict(b1, b2, p1, p2, r1, r2, time);
				}
			}
		}
		return null;
	}
	
	public static Conflict getConflictAtTime( Bot b1, Pos p1, long time, Schedule schedule, Environment env, boolean careStable ){
		
		for(Bot b2: env.getBotFleet().getBots() ){
			Conflict c = getConflictAtTime(b1, p1, time, b2, schedule, env, careStable);
			if( c!=null ) return c;
		}
		
		return null;
	}
	
	public static List<Conflict> getConflictsAtTime( Bot b1, Pos p1, long time, Schedule schedule, Environment env, boolean careStable ){
		
		List<Conflict> conflicts=null;
		
		for(Bot b2: env.getBotFleet().getBots() ){
			if( b1==b2 ) continue;
			
			Conflict c = getConflictAtTime(b1, p1, time, b2, schedule, env, careStable);
			if( c!=null ) {
				if( conflicts==null ){
					conflicts = new ArrayList<Conflict>();
				}
				conflicts.add( c );
			}
		}
		
		return conflicts;
	}
	
	public static Conflict getConflictAtTime( Bot b1, Pos p1, long time, Bot b2, Schedule schedule, Environment env, boolean careStable ){
		if( b2==b1 ) return null;
		
		Pos p2 = schedule.getBotPosition(b2, time, !careStable);
		if( p2==null ) return null;
		
		if( isConflictPosition(p1,p2,env) ){
			Route r2 = schedule.getRoute(b2, time, !careStable);
			return new Conflict(b1,b2,p1,p2,null,r2,time);
		}
		return null;
	}

	
	public static Conflict getFirstConflictAfterTime( Bot b1, Pos p1, long initTime, Schedule schedule, Environment env ){

		Conflict firstConflict = null;
		for(ScheduleItem item: schedule.getItems()){
			if( item.getCar()==b1 ) continue;
			if( item.getRoute().getFinalTime()<initTime ) continue;
			
			//TODO below code has to be made better and more efficient
			long checkStartTime = Math.max( item.getRoute().getStartTime(), initTime);
			
			long checkStep = 30;
			for(long t=checkStartTime; t<=item.getRoute().getFinalTime(); t+=checkStep){
				Pos p2 = item.getRoute().getPos(t, false);
				Bot b2 = item.getRoute().getBot();
				double d1 = Math.abs(p1.x-p2.x);
				double d2 = Math.abs(p1.y-p2.y);
				if( d1<=env.getSaveDistance() && d2<=env.getSaveDistance() ) {
					if( firstConflict==null || checkStartTime <  firstConflict.time ){
						firstConflict = new Conflict(b1, b2, p1, p2, null, item.getRoute(), t); 
						break;
					}
				}				
			}
		}
		return firstConflict;
	}
	
	public static boolean isConflictPosition( Pos p1, Pos p2, Environment env  ){
		double d1 = Math.abs(p1.x-p2.x);
		double d2 = Math.abs(p1.y-p2.y);
		return d1<=env.getSaveDistance() && d2<=env.getSaveDistance();
	}
	
	
	public static boolean verifyRoute( Route route, Schedule schedule, Environment env ){
		long startTime=route.getStartTime();
		
		Pos p = route.getPos(startTime, true);
		
		double d = Math.abs(p.x-route.getInitPosition().x) +  
				   Math.abs(p.y-route.getInitPosition().y);
		
		if( d>0.001 ) {
			return false;
		}
		
		p = route.getPos(startTime-10, true);
		
		if( p!=null ){
			return false;
		}
		
		long endTime=route.getFinalTime();
		
		p = route.getPos(endTime, true);
		
		d = Math.abs(p.x-route.getFinalPos().x) +  
		    Math.abs(p.y-route.getFinalPos().y);
		
		if( d>0.001 ) {
			return false;
		}
		
		p = route.getPos(endTime+10, true);
		
		if( p!=null ){
			return false;
		}
		
		return true;
	}
	
	public static boolean isBlockingPlace( Bot bot, Pos pos, long startTime, long endTime, Schedule schedule, Environment env ){
			
		long delTime = env.DEFAULT_TIME_ACCURACY;
		
		double xMin = pos.x - env.getSaveDistance();
		double xMax = pos.x + env.getSaveDistance();
		double yMin = pos.y - env.getSaveDistance();
		double yMax = pos.y + env.getSaveDistance();
		
		for(long time=startTime; time<endTime; time+=delTime ){
			
			if( schedule.isBotConflit1(bot, xMin, xMax, yMin, yMax, time, true) ){
				//log.error("conflicted routes: pos=" + pos.toString() + " time=" + time);
				return true;
			}
		}
	
		return false;
	}
	
	public static Conflict isFreeRoute( Route route, Schedule schedule, Environment env, boolean checkStable ){
		
		for(long time=route.getStartTime(); time<=route.getFinalTime(); time+=100){
			Pos pos = route.getPos(time, false);
			
			double xMin = pos.x - env.getSaveDistance();
			double xMax = pos.x + env.getSaveDistance();
			double yMin = pos.y - env.getSaveDistance();
			double yMax = pos.y + env.getSaveDistance();
			
			Route r2 = schedule.getConflitedRoute( route.getBot(), xMin, xMax, yMin, yMax, time, checkStable);
			if( r2==null ) continue;
			
			Pos p2 = route.getPos(time,!checkStable);
			return new Conflict(route.getBot(), r2.getBot(), pos, p2, route, r2,time );

		}

		return null;
	}
	
//	public static List<Blocker> getBlockers( Route route, Schedule schedule, Environment env ){
//		
//		Bot b1 = route.getCar();
//		
//		for(long time=route.getStartTime(); time<=route.getEndTime(); time+=100){
//			Pos pos = route.getPos(time, false);
//			
////			double xMin = pos.x - env.getSaveDistance();
////			double xMax = pos.x + env.getSaveDistance();
////			double yMin = pos.y - env.getSaveDistance();
////			double yMax = pos.y + env.getSaveDistance();
//			
//			for( Bot b2: env.getBotFleet().getBots() ){
//				
//			}
//			
//			
//			
//			Route r2 = schedule.getConflitedRoute(route.getCar(), xMin, xMax, yMin, yMax, time, checkStable);
//			if( r2==null ) continue;
//			
//			Pos p2 = route.getPos(time,!checkStable);
//			return new Conflict(route.getCar(), r2.getCar(), pos, p2, route, r2,time );
//
//		}
//
//		return null;
//	}
		
	public static Conflict haveConflicts( Schedule schedule, Environment env ){
		List<ScheduleItem> items = schedule.getItems();
		
		for( ScheduleItem si: items ){
			
			Route r = si.getRoute();
			for(long time=r.getStartTime(); time<=r.getFinalTime(); time+=100){
				Pos pos = r.getPos(time, false);
				
				double xMin = pos.x - env.getSaveDistance();
				double xMax = pos.x + env.getSaveDistance();
				double yMin = pos.y - env.getSaveDistance();
				double yMax = pos.y + env.getSaveDistance();
				
				Route r2 = schedule.getConflitedRoute(r.getBot(), xMin, xMax, yMin, yMax, time, true);
				if( r2==null ) continue;
				
				Conflict c = new Conflict( r.getBot(), r2.getBot(), pos, r2.getPos(time, false), r, r2, time);
				
				return c;
			}
		}
		
		return null;
	}

}
