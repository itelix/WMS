package cma.store.control.utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.env.Environment;
import cma.store.schedule.ScheduleItem;


/**
Warehouse optimizer.
creating date: 2012-07-26
creating time: 18:43:54
autor: Czarek
 */

public class RoutUtils {
	
	private Environment env;
	private List<Route> routes;
	private Map<Bot, List<Route>> bot2Routs;
	//private Map<Bot, Pair<Double, Double>> bot2minmaxTime;
	
	
	public RoutUtils( Environment env ){
		this.env = env;
	}
	
	public void initBotRoutes(){
		setBot2Routes();
	}
	
	private void setBot2Routes(){
		bot2Routs = new Hashtable<Bot, List<Route>>();

		for( ScheduleItem si: env.getSchedule().getItems() ){
			Route r = si.getRoute();
			List<Route> list = bot2Routs.get(r.getBot());
			if( list==null ){
				list = new ArrayList<Route>();
				bot2Routs.put(r.getBot(), list);
			}
			list.add( r );
		}	
	}
	
	public boolean colision( Pos p1, Pos p2){
		//TODO it Has to be changed to real car size
		double deltaDist = 2*env.getLayerModel().getUnitSize(); 
		return Math.abs(p1.x-p2.x )<=deltaDist  && Math.abs(p1.y-p2.y )<=deltaDist;
	}
	
	private Pos getBotPosition( Bot b, long time, boolean nullWhenAfterRoute){

		List<Route> bRoutes = bot2Routs.get(b);
		if( bRoutes==null || bRoutes.size()==0 ){
			return b.getPos();
		}
		
		Pos p = b.getPos();
		long maxTime = 0;
		Route maxRoute = null;
		for( Route r: bRoutes ){
			if( r.getStartTime() >= time ){
				continue;
			}
				
			if( r.getFinalTime()>=maxTime ){
				maxTime = r.getFinalTime();
				maxRoute = r;
			}
		}
		
		if( maxRoute!=null ){
			p = maxRoute.getPos(time, nullWhenAfterRoute);
		}
		
		Logger logger = Logger.getLogger(getClass());
		//if (maxRoute != null) logger.debug("Coliding route: " + maxRoute.printRoute());
		return p;
	}
	
	public Colision getCollision( Bot bot, Pos pos, long time ){
		for( Bot b: env.getBotFleet().getBots() ){
			if( b==bot ) continue;
			Pos p2 = getBotPosition(b,time, true);
			if( colision( p2, pos ) ) {
				List<Bot> colidingBots = new ArrayList<Bot>();
				colidingBots.add(b); colidingBots.add(bot);
				return new Colision(time, pos, colidingBots);
			}
		}
		return null;
	}
	
	public Colision getCollisionWithWorkingBot( Bot bot, Pos pos, long time ){
		for( Bot b: env.getBotFleet().getBots() ){
			if( b==bot ) continue;
			if(!b.isWork()) continue;
			Pos p2 = getBotPosition(b,time,false);
			if( colision( p2, pos ) ) {
				List<Bot> colidingBots = new ArrayList<Bot>();
				colidingBots.add(b); colidingBots.add(bot);
				return new Colision(time, pos, colidingBots);
			}
		}
		return null;
	}

	public long getMaxEndTime(long searchStartTime) {
		long maxTime = -1;
		for( Bot b: env.getBotFleet().getBots() ){
			List<Route> bRoutes = bot2Routs.get(b);
			if(bRoutes == null || bRoutes.size() == 0) {
				continue;
			}
			for( Route r: bRoutes ){
				if (r.getStartTime() >= searchStartTime){
					// this route is disjoint in time with routes
					// executed before searchStartTime
					///continue;
				}
					
				if( r.getFinalTime()>=maxTime ){
					maxTime = r.getFinalTime();
				}
			}
		}
		return maxTime;
	}
	
//	public boolean collide( Route r1, List<Route> routes ){
//		this.routes = routes;
//		setBot2Routes();
//		
//		BotFleet bf = env.getFleet();
//			
//		r1.getStartTime()
//	}
	
	

}
