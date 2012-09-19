/**
 * 
 */
package cma.store.control.opt.route.simple;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.log4j.Logger;

import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.data.ShortRouteScore;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.control.opt.route.tools.FreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Duration;
import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.data.Route;
import cma.store.data.Speed;
import cma.store.env.Environment;
import cma.store.schedule.Schedule;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;
import cms.store.utils.Pair;
import cma.store.data.PosDirected.Direction;


/**
 * @author Filip
 *
 */
public class BackwardsRouteCreator extends SimpleRouteCreator {
	public static boolean DEBUG_ON = false;
	
	Logger log = Logger.getLogger(getClass());
	
	private static final double PENALTY_STEP = 1.0;
	private static final double PENALTY_DIRECTION_CHANGE = 0.01;

	private static boolean BACKWARD_DEBUG = true;
	
	private Environment env;
	private double speed;
	private ShortRouteScore[][] scores;
	private long startTime;

	private long initialEndTime;
	
	public BackwardsRouteCreator(Environment env) {
		super(env);
		this.env = env;
	}
	
	private void initBackwards(){
		speed = bot.getMaxSpeed();
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
		
		initialEndTime = request.getToTime();
	}
	
	protected Route getRouteBackwards( Pos initPos, Pos finalPos, long startTime, boolean withEndStop, Double priority ){
		
		LinkedList<Pos> pp = new LinkedList<Pos>();
		List<Conflict> blockers = new ArrayList<Conflict>();
		
		Pos p = initPos;
		int xx1 = (int)(finalPos.x/deltaDist);
		int yy1 = (int)(finalPos.y/deltaDist);
		
		while(true){
			int xx2 = (int)(p.x/deltaDist);
			int yy2 = (int)(p.y/deltaDist);
			
			if( scores[xx2][yy2].blockers!=null ){
				blockers.addAll(0, scores[xx2][yy2].blockers);
			}
			
			pp.add(p);
			if( xx1==xx2 && yy1==yy2 ) break;
			p = scores[xx2][yy2].prior.getPos();
		}
		
		Route route = getRoute(initPos,(long)startTime,pp,withEndStop,priority,blockers);

		return route;
		
	}
	
	/**
	 * Return final position
	 * @param bot
	 * @param toCheck
	 * @return
	 */
	private Pair<PosDirected,Long> createRouteImpl( Bot bot, Queue<PosDirected> toCheck){
		this.bot = bot;
		Double bestFinalScore = null;
		PosDirected bestFinalPos = null;
		long bestFinalTime = 0;
				
		while( !toCheck.isEmpty() ){
			PosDirected pd11 = toCheck.poll();
			Pos p11 = pd11.getPos();
			ShortRouteScore s1 = scores[(int)(p11.x/deltaDist)][(int)(p11.y/deltaDist)];
						
			for(int i=0; i<Direction.values().length; i++){
				PosDirected pd2 = getNextPos( pd11, Direction.values()[i] );
				Pos p2 = pd2.getPos();
				
				// Skip if we go bad direction in directed graph
				if (directedLayoutSkip(p2, p11))
					continue;

				long timeScore = (long)(s1.time - deltaTime);
				if (timeScore < 0)
					continue;

				if( !env.getLayerModel().isRoad( p2 ) ) continue;

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
					
					toCheck.add(pd2);
					scores[xx][yy] = new ShortRouteScore(score, pd11, timeScore, blockers);
					
					if( freeRouteController.isDestination(bot, p2, timeScore ) ){
						bestFinalTime = timeScore;
						bestFinalScore = score;
						bestFinalPos = pd2;
						scores[xx][yy].setPriority( freeRouteController.getPriority() );
						
						if( DEBUG_ON ){
							verify(pd2,timeScore);
						}
					}
				}
				
				if(DEBUG_ON && BACKWARD_DEBUG) {
					verify(pd2,timeScore);
				}
				
			}		
		}
		
		
		if( DEBUG_ON && bestFinalPos!=null ){
			Schedule sh = ((ConfigurableFreeRouteController)freeRouteController).getTmpSchedule();
			Route r = getRouteBackwards( bestFinalPos.getPos(), request.getTo(), bestFinalTime, false,null);
			
			Conflict c=ConflictFinder.isFreeRoute(r, sh, env, false); 
			if( c!=null ){
				r = getRouteBackwards( request.getFrom(), bestFinalPos.getPos(), startTime, false,null);
				log.error("Conflicted route: pos=" + bestFinalPos.toString() );
				ConflictFinder.isFreeRoute(r, sh, env, false);
			}

			ConflictFinder.isFreeRoute(r, sh, env, false) ;
			ConflictFinder.isFreeRoute(r, sh, env, true) ;
		}
		
		return new Pair<PosDirected, Long>(bestFinalPos, bestFinalTime);
	}
	

	/**
	 * We know the time to
	 * @return
	 */
	private Route createRoute(){

		Pos to = new Pos(request.getTo());
		
	    scores = new ShortRouteScore[env.getLayerModel().getSizeX()][env.getLayerModel().getSizeY()];
		
		Pos startPos = null;
		startTime = initialEndTime;
		
		List<Conflict> blockers = freeRouteController.isRouteFree(bot, to, startTime, true);
		if( blockers!=null && blockers.size()>0 ) {
			List<Conflict> routesConflicts = freeRouteController.isRouteFree(bot, to, startTime, false);
			if( routesConflicts!=null && routesConflicts.size()>0 ){
				return null;  //more series problem which cant be solved, route is not correct
			}
		}
		
		ShortRouteScore s1 = new ShortRouteScore(0.0,null,(long)startTime, blockers);
		scores[(int)(to.x/deltaDist)][(int)(to.y/deltaDist)] = s1;

		Queue<PosDirected> toCheck = new LinkedList<PosDirected>();
		toCheck.add( new PosDirected(to, PosDirected.Direction.NORTH) );
		
		Pair<PosDirected, Long> pair;
		pair = createRouteImpl( bot, toCheck );
		startPos = pair.getT1().getPos();
		long finalStartTime = pair.getT2();
		
		if( startPos==null ) {
			return null;
		}

		ShortRouteScore endScore = scores[(int)(startPos.x/deltaDist)][(int)(startPos.y/deltaDist)];

		Route route = getRouteBackwards( startPos, to, finalStartTime, true, endScore.getPriority() );

		return route;
	}
	
	@Override
	public Route createRout(Request request, Bot bot, FreeRouteController freeRouteController, boolean forward) {
		return super.createRout(request, bot, freeRouteController, true);
	}
	
	public Route createRouteBackwards(Request request, Bot bot, FreeRouteController freeRouteController) {
		
		this.bot = bot;
		this.request = request;
		this.freeRouteController=freeRouteController;
		
		initBackwards();
				
		return createRoute();
	}

}
