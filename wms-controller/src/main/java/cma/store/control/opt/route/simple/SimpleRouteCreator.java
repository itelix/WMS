package cma.store.control.opt.route.simple;

import java.nio.file.attribute.PosixFilePermission;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import cma.store.config.SettingProperties;
import cma.store.control.Controller;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.allshortestpaths.TravelTimeMap;
import cma.store.control.opt.route.data.ShortRouteScore;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.control.opt.route.tools.FreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.LayerModel.AVENUE_TYPE;
import cma.store.data.Mvc;
import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.data.PosDirected.Direction;
import cma.store.data.Route;
import cma.store.data.RouteImp;
import cma.store.data.Speed;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.schedule.Schedule;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;
import cma.store.utils.Utils;
import cms.store.utils.Pair;
import cms.store.utils.PositionUtils;


/**
Warehouse optimizer.
creating date: 2012-07-15
creating time: 18:22:04
autor: Czarek
 */

public class SimpleRouteCreator implements RouteCreator {
	public static boolean DEBUG_ON = true;
	
	Logger log = Logger.getLogger(getClass());
	
	private static final double PENALTY_STEP = 1.0;
	private static final double PENALTY_DIRECTION_CHANGE = 0.01;

	private boolean REALISTIC_TIMES;
	
	private Environment env;
	private double speed;
	protected double deltaDist;
	protected double deltaTime;
	private ShortRouteScore[][] scores;
//	private boolean checkCars;
//	private RoutUtils routUtils;
	protected Request request;
	protected Bot bot;
	protected FreeRouteController freeRouteController;
	private Long initialStartTime = null;
	private long startTime;
	private boolean forward;
	
	public SimpleRouteCreator( Environment env ){
		this.env = env;
		REALISTIC_TIMES = Boolean.parseBoolean((String) SettingProperties
                .getInstance().getValue("external.properties",
                        Controller.REALISTIC_TIMES)); 
	}
	

	@Override
	public Route createRout(Request request, Bot bot, FreeRouteController freeRouteController, boolean forward) {
		
		this.bot = bot;
		this.request = request;
		this.freeRouteController=freeRouteController;
		this.forward=forward;
		
		init();
				
		return createNoAccidentRoute();
	}
	
	private void init(){
		speed = bot.getMaxSpeed();
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
		
		if( request.getFromTime()!=null ){
			initialStartTime = request.getFromTime();
		}else{
			initialStartTime = request.getToTime() 
					- Utils.minTimeToTrevel(bot, request.getFrom(), request.getTo());
		}
		
//		if( checkCars ){
//			routUtils = new RoutUtils(env);
//			routUtils.initBotRoutes();
//		}
	}

	protected PosDirected getNextPos( PosDirected oldDir, Direction d){
		Pos old = oldDir.getPos();
		switch(d){
			case EAST: return new PosDirected(new Pos(old.x+deltaDist,old.y), PosDirected.Direction.EAST);
			case WEST: 	return new PosDirected(new Pos(old.x-deltaDist,old.y), PosDirected.Direction.WEST);
			case NORTH: 	return new PosDirected(new Pos(old.x,old.y+deltaDist), PosDirected.Direction.NORTH);
			case SOUTH: 	return  new PosDirected(new Pos(old.x,old.y-deltaDist), PosDirected.Direction.SOUTH);
			default: throw new RuntimeException( "Direction isn't support yet");
		}
	}
	
	protected boolean isDirectionChange( Pos p1, Pos p2, Pos p3 ){
		return (p2.x-p1.x != p3.x-p2.x) || (p2.y-p1.y != p3.y-p2.y);
	}
	
	protected boolean directedLayoutSkip(Pos p1, Pos p2) {
		return ((env.getLayerModel().getAvenueType(p1) == AVENUE_TYPE.EAST
				&& p2.x - p1.x < 0)
				|| (env.getLayerModel().getAvenueType(p1) == AVENUE_TYPE.WEST
						&& p2.x - p1.x > 0));
	}
	
	/**
	 * Return final position
	 * @param bot
	 * @param toCheck
	 * @return
	 */
	private PosDirected findRoad( Bot bot, Queue<PosDirected> toCheck){
		this.bot = bot;
		Double bestFinalScore = null;
		PosDirected bestFinalPos = null;
		long bestTime = 0;
//		int toxx = (int)(to.x/deltaDist);
//		int toyy = (int)(to.y/deltaDist);
				
		while( !toCheck.isEmpty() ){
			PosDirected pd11 = toCheck.poll();
			Pos p11 = pd11.getPos();
			ShortRouteScore s1 = scores[(int)(p11.x/deltaDist)][(int)(p11.y/deltaDist)];
						
			for(int i=0; i<Direction.values().length; i++){
				PosDirected pd2 = getNextPos( pd11, Direction.values()[i] );
				Pos p2 = pd2.getPos();
				
				// Skip if we go bad direction in directed graph
				if (directedLayoutSkip(p11, p2))
					continue;

				if( !env.getLayerModel().isRoad( p2 ) ) continue;
				
				double stepTime = deltaTime;
				long timeScore = s1.time;
				if (REALISTIC_TIMES) {
					stepTime = TravelTimeMap.getInstance(env).get90DegreeRotationTime(pd11, pd2);
					timeScore += (long)stepTime;
					if (stepTime > 0) {
						// We must reserve p11 position during rotation time
						List<Conflict> blockers = freeRouteController.isRouteFree(bot, p11, timeScore, true);
						if( blockers!=null && blockers.size()>0 ) {
							List<Conflict> routesConflicts = freeRouteController.isRouteFree(bot, p11, timeScore, false);
							if( routesConflicts!=null && routesConflicts.size()>0 ){
								continue;  //more series problem which cant be solved, route is not correct
							}
						}
					}
					stepTime += TravelTimeMap.getInstance(env).getNeighbourTravelTimeNoRatations(pd11, pd2);
				}
				
				if (stepTime > 100000) {
					TravelTimeMap.getInstance(env).getTravelTime(p11, p2);
					log.debug("110000");
				}
				
				timeScore = (long)(s1.time + stepTime);
				
				List<Conflict> blockers = freeRouteController.isRouteFree(bot, p2, timeScore, true);
				if( blockers!=null && blockers.size()>0 ) {
					List<Conflict> routesConflicts = freeRouteController.isRouteFree(bot, p2, timeScore, false);
					if( routesConflicts!=null && routesConflicts.size()>0 ){
						continue;  //more series problem which cant be solved, route is not correct
					}
				}
				
				double score = s1.score - PENALTY_STEP;
				
				if( s1.prior!=null ){
					if( isDirectionChange(s1.prior.getPos(), p11, p2) ){
						score -= PENALTY_DIRECTION_CHANGE;
					}
				}
				
				if( bestFinalScore!=null && score<bestFinalScore ){
					continue;
				}
				
				int xx = (int)(p2.x/deltaDist);
				int yy = (int)(p2.y/deltaDist);
				
				ShortRouteScore s2 = scores[xx][yy];
				
				if( s2==null || s2.score < score ){
					
					//System.out.println("Add Pos=" + p2 + " score = " + score );
					toCheck.add(pd2);
					scores[xx][yy] = new ShortRouteScore(score, pd11, timeScore, blockers);
					
//					if( xx==toxx && yy==toyy ){
//						bestFinalScore = score;
//					}
					
					if( freeRouteController.isDestination(bot, p2, timeScore ) ){
						bestFinalScore = score;
						bestFinalPos = pd2;
						bestTime = timeScore;
						
						
						scores[xx][yy].setPriority( freeRouteController.getPriority() );
						
						if( DEBUG_ON ){
							verify(pd2,timeScore);
						}
						//log.error("BEST:: No conflicted route: pos=" + p2.toString() + " time=" + timeScore + " route="+r);
					}
				}
				else if (s1.score < score) {
					toCheck.add(pd2);
				}
				
				if(DEBUG_ON){
					verify(pd2,timeScore);
				}
				
			}		
		}
		
		
		if( DEBUG_ON && bestFinalPos!=null ){
			verify(bestFinalPos,bestTime);
		}
		
		return bestFinalPos;
	}
	
	protected void verify( PosDirected pd2, long timeScore ){
		Pos p2 = pd2.getPos();
		Schedule sh = ((ConfigurableFreeRouteController)freeRouteController).getTmpSchedule();
		Route r = getRoute( request.getFrom(), p2, startTime, false,null);
		//log.error("No conflicted route: pos=" + p2.toString() + " time=" + timeScore + " route="+r);
		
		Conflict c=ConflictFinder.isFreeRoute(r, sh, env, false); //TODO debug only
		if( c!=null ){
			r = getRoute( request.getFrom(), p2, startTime, false,null);
			//log.error("Conflicted route: pos=" + p2.toString() + " time=" + timeScore);
			ConflictFinder.isFreeRoute(r, sh, env, false);
			freeRouteController.isRouteFree(bot, p2, timeScore, false );
		}
	}
	
	
	/**
	 * We know the time to
	 * @return
	 */
	private Route createNoAccidentRoute(){

		Pos from = new Pos(request.getFrom());
		PosDirected fromDir = new PosDirected(from, PosDirected.Direction.WEST);
		//Pos to = new Pos(request.getTo());
		
	    scores = new ShortRouteScore[env.getLayerModel().getSizeX()][env.getLayerModel().getSizeY()];
		
	    PosDirected finalPosDir = null;
		Pos finalPos = null;
//		final double startTime = request.getFromTime();		
		startTime = initialStartTime;

		List<Conflict> blockers = freeRouteController.isRouteFree(bot, from, startTime, true);
		if( blockers!=null && blockers.size()>0 ) {
			List<Conflict> routesConflicts = freeRouteController.isRouteFree(bot, from, startTime, false);
			if( routesConflicts!=null && routesConflicts.size()>0 ){
				return null;  //more series problem which cant be solved, route is not correct
			}
		}
		
		ShortRouteScore s1 = new ShortRouteScore(0.0,null,(long)startTime,blockers);
		scores[(int)(from.x/deltaDist)][(int)(from.y/deltaDist)] = s1;

		Queue<PosDirected> toCheck = new LinkedList<PosDirected>();
		toCheck.add( new PosDirected(from, null) );
		
		finalPosDir = findRoad( bot, toCheck );
		if (finalPosDir == null) {
			return null;
		}
		finalPos = finalPosDir.getPos();
		//if( finalPos!=null ) break;
		//startTime+=deltaTime;
		
		
		if( finalPos==null ) {
			return null;
			//throw new RuntimeException("Can't find good rout");
		}
		
		ShortRouteScore endScore = scores[(int)(finalPos.x/deltaDist)][(int)(finalPos.y/deltaDist)];
		
		Route route;
		if (REALISTIC_TIMES) {
			route = getRouteWithRealisticTimes(fromDir, finalPosDir, startTime, endScore.time, true, endScore.getPriority());
		}
		else {
			route = getRoute( from, finalPos, startTime, true, endScore.getPriority() );
		}
		
		return route;
	}
	
	private void printPosList(int currPosX, int currPosY) {
		ShortRouteScore s = scores[currPosX][currPosY];
		Pos p = s.prior.getPos();
		while ((p = s.prior.getPos()) != null) {
			log.debug("p: " + p + "time: " + s.time);
			s = scores[(int)(p.x/deltaDist)][(int)(p.y/deltaDist)];
		}
	}
	
	private Route getRouteWithRealisticTimes(
			PosDirected initPosDir, PosDirected finalPos,
			long startTime, long endTime,
			boolean withEndStop, Double priority
	) {
		List<Conflict> blockers = new ArrayList<Conflict>();
		LinkedList<Pair<Speed, Duration>> speedDurList = new LinkedList<Pair<Speed,Duration>>();
		
		PosDirected pd = finalPos;
		Pos p = pd.getPos();
		Pos initPos = initPosDir.getPos();
		int initPosX = (int)(initPos.x/deltaDist);
		int initPosY = (int)(initPos.y/deltaDist);
		
		int currPosX = (int)(p.x/deltaDist);
		int currPosY = (int)(p.y/deltaDist);
		
		ShortRouteScore prevScore = scores[currPosX][currPosY];
		PosDirected prevPd = finalPos;
		Pos prevP = prevPd.getPos();
		ShortRouteScore score = scores[currPosX][currPosY];
		p = score.prior.getPos();
		currPosX = (int)(p.x/deltaDist);
		currPosY = (int)(p.y/deltaDist);
		score = scores[currPosX][currPosY];
		while (true) {
			if (score.blockers!=null) {
				blockers.addAll(score.blockers);
			}

			long stepTime = prevScore.time - score.time;
			long rotationTime = (long)TravelTimeMap.getInstance(env).get90DegreeRotationTime(pd, prevPd); 
			stepTime = stepTime - rotationTime;
			Speed s = new Speed(
				(prevP.x - p.x)/stepTime,
				(prevP.y - p.y)/stepTime
			);

			Duration d = new Duration(score.time, prevScore.time - rotationTime);
			Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s, d);
			speedDurList.addFirst(pair);
			
			if (rotationTime > 0) {
				s = new Speed(0, 0);
				d = new Duration(prevScore.time - rotationTime, prevScore.time);
				pair = new Pair<Speed, Duration>(s, d);
				speedDurList.addFirst(pair);
			}
			
			if (initPosX == currPosX && initPosY == currPosY)
				break;
			
			prevScore = score;
			prevP = p;
			prevPd = pd;
			
			pd = score.prior;
			p = pd.getPos();
			currPosX = (int)(p.x/deltaDist);
			currPosY = (int)(p.y/deltaDist);
			score = scores[currPosX][currPosY];
		}
		
		if( withEndStop ){
			Pair<Speed, Duration> pair =
				new Pair<Speed, Duration>(
					new Speed(0,0),
					new Duration(endTime, endTime + request.getStayAtFinal())
				);
			speedDurList.add(pair);
		}
		
		RouteImp route = new RouteImp(bot, speedDurList, null, initPos, startTime, request);
		route.addBlockers( blockers );
		return route;		
	}
	
	private Route getRoute( Pos initPos, Pos finalPos, long startTime, boolean withEndStop, Double priority ){
		
		LinkedList<Pos> pp = new LinkedList<Pos>();
		List<Conflict> blockers = new ArrayList<Conflict>();
		
		Pos p = finalPos;
		int xx1 = (int)(initPos.x/deltaDist);
		int yy1 = (int)(initPos.y/deltaDist);
		
		while(true){
			int xx2 = (int)(p.x/deltaDist);
			int yy2 = (int)(p.y/deltaDist);
			
			if( scores[xx2][yy2].blockers!=null ){
				blockers.addAll( scores[xx2][yy2].blockers );
			}
			
			pp.addFirst(p);
			
			if( xx1==xx2 && yy1==yy2 ) break;
			p = scores[xx2][yy2].prior.getPos();
			
		}
		
		Route route = getRoute(initPos,(long)startTime,pp,withEndStop,priority,blockers);
//		log.info("Route create " + route.getStartTime() + " "
//				+ route.getEndTime());
		return route;
		
	}
	
	protected Route getRoute( Pos from, long startTime, List<Pos> pp, boolean withEndStop, Double priority, List<Conflict> blockers ){
		
		List<Pair<Speed, Duration>> speedDurList = new ArrayList<Pair<Speed,Duration>>();
		
		long priorTime = startTime;
		
		if( pp.size()>1){
			Pos prior = pp.get(0);
			for(int i=1; i<pp.size(); i++){
				Pos next = pp.get(i);
				Speed s = new Speed( (next.x-prior.x)/deltaTime, (next.y-prior.y)/deltaTime );
				
				long nextTime = (long)(priorTime+deltaTime);
				
				Duration d = new Duration( priorTime, nextTime);
				Pair<Speed, Duration> pair = new Pair<Speed, Duration>(s,d);
				speedDurList.add(pair);
				prior=next;
				priorTime=nextTime;
			}
		}
		
		if( withEndStop ){
			Pair<Speed, Duration> pair = new Pair<Speed, Duration>( new Speed(0,0), new Duration(priorTime, priorTime+request.getStayAtFinal()) );
			speedDurList.add(pair);
		}
		
		RouteImp route = new RouteImp(bot,speedDurList,pp,from,startTime,request);	
		route.addBlockers( blockers );
		
		return route;
	}


}
