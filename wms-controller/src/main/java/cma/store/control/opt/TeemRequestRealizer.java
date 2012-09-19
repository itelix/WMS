package cma.store.control.opt;

import java.util.List;

import org.apache.log4j.Logger;

import cma.store.control.mvc.MvcDeliveryTimeCalculator;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteAbs;
import cma.store.data.RouteImp;
import cma.store.env.Environment;
import cma.store.exception.NoFreeBotException;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.ConflictFinder;
import cma.store.utils.Utils;

/**
Warehouse optimizer.
creating date: 2012-07-18
creating time: 20:40:20
autor: Czarek
 */

public class TeemRequestRealizer {
	Logger log = Logger.getLogger(getClass());
	
	private Environment env;
	//private List<LocPriority> productLocations;
	RouteCreator routeCreator;
	//TeamPlaninngContext planinngContext = new TeamPlaninngContext(new NearestFreeBotStrategy(env));
	MvcDeliveryTimeCalculator mvcDeliveryTimeCalculator;

	public TeemRequestRealizer( Environment env ){
		this.env = env;
		this.routeCreator = env.getRouteCreator();
	}
	
	private void init(){
		if( mvcDeliveryTimeCalculator==null ){
			mvcDeliveryTimeCalculator = env.getMvcDeliveryTimeCalculator();
		}		
	}
	
	
	private void findRealizationsMoves( List<BaseRequest> baseRequestList ) throws NoFreeBotException {
		
		for(BaseRequest br: baseRequestList){
			
			int routeGroupId = RouteAbs.getNextRouteId();
			
			long endTime = env.getSchedule().getMvcAvailableTime( br.getMvc() );
			
			BotProdLocation bp = findBestBotAndProductLoction(br,endTime);
			if( bp==null )  throw new NoFreeBotException(br);  //shouldn happens
			
			long minTimeToTrevel = minTimeToTrevel( bp.getBot(), bp.getProdPos(), br.getDestination() );
			long firstRouteEndTime = endTime - Environment.TIME_TO_PICK_UP_PRODUCT - minTimeToTrevel;
			
			Request r1 = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, null, bp.getBotStartPos(), firstRouteEndTime, bp.getProdPos(), 0, br );
			ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r1,false,true);
			Route route1  = routeCreator.createRout( r1, bp.getBot(), c1, true );
			if( route1==null) {
				//recoverAfterFailedRouteSearch(cp.getCar());
				throw new NoFreeBotException(br);  
			}
			
			long startTimeRoute2 = route1.getFinalTime() + Environment.TIME_TO_PICK_UP_PRODUCT;
			Request r2 = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, startTimeRoute2, bp.getProdPos(), null, br.getMvc().getPos(), 0, br );
			ConfigurableFreeRouteController c2 = new ConfigurableFreeRouteController(env,r2,false,true);
			Route route2  = routeCreator.createRout( r2, bp.getBot(), c2, true );
			if( route2==null) {
				//recoverAfterFailedRouteSearch(cp.getCar());
				throw new NoFreeBotException(br);  
			}
			
			route1.setRouteGroupId(routeGroupId);
			route2.setRouteGroupId(routeGroupId);
			
			env.getSchedule().add( new ScheduleItem(route1) );
			env.getSchedule().add( new ScheduleItem(route2) );
			
			moveBlokersBots( bp.getBot(), br, routeGroupId );

		}
	}
	
	/**
	 * We have to remember all added routes to have a chance to remove them and check different.
	 * @throws NoFreeBotException
	 */
	private boolean moveBlokersBots( Bot dontCheck, BaseRequest br, int routeGroupId, boolean careStableBots ) throws NoFreeBotException {
		boolean anyMove = false;
		
		for( Bot bot: env.getBotFleet().getBots() ){
			
			if( dontCheck == bot ) continue;
			
			Pos pos;
			long time;
			Route r = env.getSchedule().getLastRoute( bot );
			if( r==null ){
				pos = bot.getPos();
				time = 0;
			}else{
				pos = r.getFinalPos();
				time = r.getFinalTime();
			}

			
			if( ConflictFinder.getFirstConflictAfterTime(bot, pos, time, env.getSchedule(), env )!=null ){
				anyMove=true;
				Request request = new Request( RequestType.KNOWN_START_AND_TIME, time, pos, null, null, 0, br );
				ConfigurableFreeRouteController c = new ConfigurableFreeRouteController(env,request,careStableBots,true);
				
				Route route  = routeCreator.createRout( request, bot, c, true );
				if( route==null) {
					//recoverAfterFailedRouteSearch(cp.getCar());
					throw new NoFreeBotException(br);  
				}
				
				route.setRouteGroupId(routeGroupId);
				env.getSchedule().add( new ScheduleItem(route) );
				
			}
		}
		
		return anyMove;
	}
	
	/**
	 * We have to remember all added routes to have a chance to remove them and check different.
	 * @throws NoFreeBotException
	 */
	private boolean moveBlokersBots( Bot dontCheck, BaseRequest br, int routeGroupId ) throws NoFreeBotException {
		int maxIter=10;
		
		for(int i=0; i<maxIter; i++){
			if( !moveBlokersBots( dontCheck, br, routeGroupId, false ) ){
				break; //no changes
			}
			if( !moveBlokersBots( dontCheck, br, routeGroupId, true ) ){
				break; //no changes
			}				
		}
		
		return true;
		
	}
		

	
	public List<RequestRealization> findRealizations( List<BaseRequest> baseRequestList ) throws NoFreeBotException {
		
		init();
		
		//find routes and don't care about stable (not moveable) bots.
		findRealizationsMoves( baseRequestList);
		
		
		return null;
	}
	
//	private long getMaxTrevelTime( Bot bot ){
//		double x = env.getModel().getLayerModel().getRealSizeX();
//		double y = env.getModel().getLayerModel().getRealSizeY();
//		return minTimeToTrevel( bot, new Pos(0,0), new Pos(x,y) ) * 2;
//	}
	
	private BotProdLocation findBestBotAndProductLoction( BaseRequest baseRequest, long endTime ){
		
		long deltaTime = 1000;
		long startTime = endTime - deltaTime ;
		List<Bot> bots;
		
		do{
			startTime+=deltaTime;
			bots = env.getSchedule().getAvailableCars( startTime, baseRequest.getDestination(), env.getBotFleet().getBots() );
			
		}while( bots.size() == 0 );
		
		return getClosestAvailableCarAndProduct( bots, baseRequest.getStorageLocations(), startTime);
	}
	
	private long minTimeToTrevel( Bot bot, Pos pos1, Pos pos2 ){
		return Utils.minTimeToTrevel(bot, pos1, pos2);
	}
	
	
//	private long getTimeToTrevel( Pos p1, Pos p2, Bot car){
//		double speed = car.getMaxSpeed();
//		double dist = Math.abs( p1.x-p2.x ) + Math.abs( p1.y-p2.y );
//		return (long) (speed * dist);
//	}
	
	private BotProdLocation getClosestAvailableCarAndProduct( List<Bot> bots, List<LocPriority> productLocations, long initTime ){
		
		double bestDist = Double.MAX_VALUE;
		int bestI=-1;
		int bestJ=-1;
		
		boolean used[][] = new boolean[bots.size()][productLocations.size()];
		
		for(int i=0;i<bots.size();i++){
			for(int j=0;j<productLocations.size();j++){
				if( used[i][j] ) continue;
				//double d = distance( productLocations.get(j), cars.get(i));
				double d = Utils.distance( productLocations.get(j), bots.get(i));
				if( d<bestDist ){
					bestDist = d;
					bestI = i;
					bestJ = j;
				}
			}
		}
		
		if( bestI == -1 ) return null;
		used[bestI][bestJ] = true;
		
		Pos carPos = env.getBotPositionPredictor().getCarPosition( bots.get(bestI), initTime, false);
		//productLocations.remove(bestJ);
		return new BotProdLocation( bots.get(bestI), productLocations.get(bestJ).getPos(), carPos );
	}


}
