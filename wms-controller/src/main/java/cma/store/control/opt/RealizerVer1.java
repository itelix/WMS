package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.control.BaseRealization;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.ConflictFinder;

public class RealizerVer1 extends RandomRealizer {
	private static final int NEXT_SOLUTION_TIME_DELTA = 500;
	private Route firstRoute;
	//protected List<ScheduleItem> currentRouteItems;

	public RealizerVer1(Environment env) {
		super(env);
	}
	
	@Override
	protected Bot chooseBot( int it ){
		
		Bot best = null;
		long endTime = Long.MAX_VALUE;
				
		for( Bot bot: env.getBotFleet().getBots() ){
			long available = getBotAvailableTime( bot );
			if( best==null || available < endTime ){
				best = bot;
				endTime = available;
			}
		}

		return best;
	}
	
	@Override
	protected boolean findRoutes( BaseRealization br ){
		 
		long startTime = br.getBotAvailable();
//		long minTimeAtMvc = br.getMinimalEndTime(); 
//		long maxStartTime = minTimeAtMvc; //TODO has to be change
		
//		while(startTime >= maxStartTime)//TODO has to be change
//			maxStartTime += 100;

		long mvcAvailableTime = getLocSchedule().getMvcAvailableTime( br.getBaseRequest().getMvc() );
		long maxStartTime = Math.max( startTime+10*NEXT_SOLUTION_TIME_DELTA, mvcAvailableTime+3600*1000);
		

		for( ; startTime <= maxStartTime; startTime+=NEXT_SOLUTION_TIME_DELTA){
		
			 Route r1 = routeFirst( br, Environment.TIME_TO_PICK_UP_PRODUCT, startTime );
			 
			 if( r1==null ) continue;
			 if( !ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env) ){
				log.error("Wrong route 1");
			 }
			 
			 long startTimeRoute2 = r1.getFinalTime();
			 Route r2 = routeSecond( br, Environment.TIME_TO_STAY_IN_MVC, startTimeRoute2 );
			 if( r2==null ) continue;
			 if( !ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env) ){
				log.error("Wrong route 2");
			 }
			 
			 addRoutes(r1,r2,br);
			 
			 boolean blokersRemoved =  moveBlokersBots( br.getBot(), br) ;
			 
			 if( blokersRemoved ){
				 return true;//solved
			 }
			 //No solution yet

			 //moveBlokersBots( br.getBot(), br, routeGroupId ) ;
			 realization.removeScheduleItems( br.getItems() );
		}
		br.getBaseRequest().setTimeToReschedule(maxStartTime+1000);
		return false;
	}
	
//	@Override
//	protected Pair<Route, Route> findRoutes( BaseRealization br ){
//		
//		long startTime = br.getBotAvailable();
//		long minTimeAtMvc = br.getMinimalEndTime(); 
//		long maxStartTime = minTimeAtMvc+40000; //TODO has to be change
//		
//		for( ; startTime <= maxStartTime; startTime+=100){
//		
//			 Route r1 = routeFirst( br, Environment.TIME_TO_PICK_UP_PRODUCT, startTime );
//			 if( r1==null ) continue;
//			 if( !ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env) ){
//				log.error("Wrong route 1");
//			 }
//			 
//			 
//			 Route r2 = routeSecond( br, r1, Environment.TIME_TO_STAY_IN_MVC );
//			 if( r2==null ) continue;
//			 if( !ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env) ){
//				log.error("Wrong route 2");
//			 }
//			 
//			 
//			protected boolean moveBlokersBots( Bot movedBot, List<Route> routsToCheck, BaseRealization br, int routeGroupId ){
//
//		 
//			 return new Pair<Route, Route>(r1,r2);
//		}
//		 
//		 return null;
//	}
	
	
	@Override
	protected final Route routeFirst( BaseRealization br, long timeToStay, long startTime ){
		//currentRouteItems = new ArrayList<ScheduleItem>();
		
		BaseRequest req = br.getBaseRequest();
		List<LocPriority> locPriority = br.getProdLocPtiority();
		Pos p1 = getLastPos(br.getBot());
		log.info("AS Order " + br.getBaseRequest().getOrderId() + " Start POS"
				+ p1 + " for Bot " + br.getBot().getId());

//		Route lastRoute= getLastRoute(br.getBot());
//		if(lastRoute != null && lastRoute.getType().equals(RouteType.ROUTE_1)) {
//			p1 = req.getDestination();
//		}
		
		//Pos p2 = br.getCarProdLocation().getProdPos();
		
		Request r = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, startTime, p1, null, null, locPriority, timeToStay, req );

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());
		
		Route route = getRouteCreator().createRout( r, br.getBot(), c1, FORWARD_ROUTE );
		if( route!=null ) {
			route.setType(RouteType.ROUTE_1);
			firstRoute = route;
			return route;
		}
		
		firstRoute=null;
		return null;
	}
	
	@Override
	protected Route routeSecond( BaseRealization br, long timeToStay, long startTime ){
		//currentRouteItems = new ArrayList<ScheduleItem>();
		
		BaseRequest req = br.getBaseRequest();
		Pos p1 = getLastPos(br.getBot());
//		Pos p1 = firstRoute.getFinalPos();
		Pos p2 = req.getMvc().getPos();
			
		Request r = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, startTime, p1, null, p2, null, timeToStay, req );
		r.setBaseRequest(req);

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());
		Route route = getRouteCreator().createRout( r, br.getBot(), c1, FORWARD_ROUTE );
		
		if( route!=null ){
			br.setDestinationPriority( c1.getDestinationPriority() );
			route.setType(RouteType.ROUTE_2);
			return route;
		}
		return null;
	}
	
	
	protected final Route shortestRouteSecond( BaseRealization br, long timeToStay ){
		//currentRouteItems = new ArrayList<ScheduleItem>();
		
		BaseRequest req = br.getBaseRequest();
		Pos p1 = getLastPos(br.getBot());
		Pos p2 = req.getMvc().getPos();
		
		//Pos p2 = br.getCarProdLocation().getProdPos();
		
		Request r = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, 0l, p1, null, p2, null, timeToStay, req );


		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,false,getLocSchedule());
		Route route = getRouteCreator().createRout( r, br.getBot(), c1, FORWARD_ROUTE );

		return route;
	}

}
