package cma.store.control.opt.route;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cma.store.control.Direction;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.tools.FreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteImp;
import cma.store.data.Speed;
import cma.store.env.Environment;
import cms.store.utils.Pair;


/**
Warehouse optimizer.
creating date: 2012-07-15
creating time: 18:22:04
autor: Czarek
 */

public class NoAccidentsSimpleRouteCreator implements RouteCreator {
	
//	private LayerModel model;
//	private Schedule shedule;
	private Random rnd;
	private Environment env;
	private static boolean checkCars;
	double speed;
	double deltaDist;
	double deltaTime;
	
	public NoAccidentsSimpleRouteCreator( Environment env ){
		this.env = env;
		rnd = new Random(env.getSeed());
	}
	
	private void init(){
		speed = env.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
	}
	

	private Pos getNextPos( Pos old, Direction d){
		
		switch(d){
			case RIGHT: return new Pos(old.x+deltaDist,old.y);
			case LEFT: 	return new Pos(old.x-deltaDist,old.y);
			case UP: 	return new Pos(old.x,old.y+deltaDist);
			case DOWN: 	return  new Pos(old.x,old.y-deltaDist);
			default: throw new RuntimeException( "Direction isn't support yet");
		}
	}
	
	private boolean recursiveRoad( List<Pair<Speed, Duration>> speedDurList, List<Pos> pp, 
			Pos act, Pos to, int initDir, int nr, double time ){
		
		if( nr>100 ){
			return false;
		}
		
		if( to.dist(act)==0 ) return true;
		
		//closer
		for(int j=0; j<=1; j++){
			for(int i=0; i<Direction.values().length; i++){
				int d = (i+initDir) % Direction.values().length;
				
				Pos p = getNextPos( act, Direction.values()[d] );
				
				if( !env.getLayerModel().isRoad( p ) ) continue;
				
				if( j==0 ){ 
					if( to.dist(p) > to.dist(act) ){
						continue;
					}
				}
				
				if( pp.contains(p) ){
					continue;
				}
				
//				if( checkCars ){
//					if( env.getSchedule().isCar( x-1, x+1, y-1, y+1, (long)time ) ){
//						continue;
//					}
//				}
				
				int id = rnd.nextInt( Direction.values().length );
				pp.add(p);
				
				boolean ok = recursiveRoad( speedDurList, pp, p, to, id, nr+1, time+1 );
				if( ok ) return true;
				
				pp.remove(pp.size()-1);
			}
		}
		return false;
	}
	
	private Route createNoAccidentRoute( Request r, Bot car ){

		Pos from = new Pos(r.getFrom());
		Pos to = new Pos(r.getTo());
		
		int id = rnd.nextInt( Direction.values().length );
		
		boolean found = false;
		final double startTime = r.getFromTime();
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();
		List<Pos> pp = new ArrayList<Pos>();
		pp.add( from );
		
		double time = startTime;
		while(!found){
			pp.clear();
			speedDurList.clear();
			found = recursiveRoad( speedDurList, pp, from, to, id, 0, time );
			time+=deltaTime;
		}
		
		if( !found ) {
			return null;
			//throw new RuntimeException("Can't find good rout");
		}

		
		if( pp.size()>1){
			double priorTime = startTime;
			Pos prior = pp.get(0);
			for(int i=1; i<pp.size(); i++){
				Pos next = pp.get(i);
				Speed s = new Speed( (next.x-prior.x)/deltaTime, (next.y-prior.y)/deltaTime );
				double nextTime = priorTime+deltaTime;
				Duration d = new Duration( (long)priorTime, (long)nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s,d);
				speedDurList.add(pair);
				prior=next;
				priorTime=nextTime;
			}
		}
		
		Route route = new RouteImp(car,speedDurList,pp,from,0,r);
		
//		for(int i=0; i<1000; i++){
//			long t = (long)(env.getTimeMs() + i*deltaTime/10);
//			
//			Pos ppp = route.getPos(t);
//			
//			System.out.println( "xxx: "+ppp );
//			
//		}
		
		return route;			
	}


	@Override
	public Route createRout(Request r, Bot car, FreeRouteController freeRouteController, boolean forward) {
		
		init();
		
		return createNoAccidentRoute(r,car);
	}

}
