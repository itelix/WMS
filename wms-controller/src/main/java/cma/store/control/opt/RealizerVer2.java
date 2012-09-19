package cma.store.control.opt;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import cma.store.control.BaseRealization;
import cma.store.control.Realization;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.opt.route.allshortestpaths.TravelTimeMap;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.Pos;
import cma.store.data.PosDirected;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.exception.NoFreeBotException;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;

public class RealizerVer2 extends RealizerVer1 {
	
	private static final int NEXT_SOLUTION_TIME_DELTA = 500;
	private List<BaseRealization> all;
	private int requestListId;
	private List<Bot> botsToChose;
	private int subListSize;
	private ShortestPathsFinder shortestPathsFinder;

	public RealizerVer2(Environment env) {
		super(env);
		//shortestPathsFinder = FloydWarshallAlgorithm.getInstance(env);
	}

	
	private List<BaseRealization> getNextRequestsList(){
		
		requestListId++;
		
		int count = all.size();
		
		int start = requestListId*subListSize;
		int end = (requestListId+1)*subListSize - 1;
		
		if( start>=all.size() ) return null;
		
		List<BaseRealization> ret = new ArrayList<BaseRealization>();
		for(int i=start; i<=end && i<count; i++ ){
			ret.add( all.get(i) );
		}

		
		return ret;
	}
	
	@Override
	protected Bot chooseBot( int it ){
		
		List<Bot> best = null;
		long endTime = Long.MAX_VALUE;
				
		for( Bot bot: botsToChose ){
			long available = getBotAvailableTime( bot );
//			double pathSize = shortestPathsFinder.computePaths();
			
			if( best==null || available < endTime ){
				best = new ArrayList<Bot>();
				best.add( bot );
				endTime = available;
			}else if( available == endTime ){
				best.add( bot );
			}
		}
		
		if( best==null ) return null;
		
		Bot bestBot = best.get( rnd.nextInt(best.size() ) );

		botsToChose.remove( bestBot );

		return bestBot;
	}
	
	@Override
	protected void updateBot( BaseRealization br, int it ){
//		Bot bot = chooseBot( it );
//		br.setBot(bot);
		Bot bot = br.getBot();
		
		if(bot == null) {
			bot = chooseBot(it);
			br.setBot(bot);
			log.error("Bot should not be empty!!!");
		}
		br.setBotStartPosition( getLastPos(bot) );
		br.setBotAvailable( getBotAvailableTime(bot) );
	}
	
	@Override
	protected void updateProdLocPriority( BaseRealization br ){
		
		List<LocPriority> list = new ArrayList<LocPriority>();
		list.addAll(br.getBaseRequest().getStorageLocations()); //could be choosed differently
		
		br.setProdLocPtiority( list );
	}
	
	
	@Override
	protected boolean findRealization(int it) throws RealizationException {
		
//		if( shortestPathsFinder==null ){
//			shortestPathsFinder = new FloydWarshallAlgorithm(env);
//		}
		
		
		createNewRealization();
//		assignBots();
		//debug
		
		all = realization.getBaseRealList();
		requestListId=-1;
		subListSize = Math.min( 3,  all.size() );
		
		List<BaseRealization> list;
		for( int j=0; j<all.size(); j+=subListSize){
			list = getNextRequestsList();
			if( list==null ) break;		
			
			botsToChose = new ArrayList<Bot>();
			botsToChose.addAll( env.getBotFleet().getBots() );
			
			//look for bes realizations;
			for( int i=0; i<list.size(); i++){
				 BaseRealization br = list.get(i);
				 updateProdAndBot(br,it);
			}
			

			for( int i=0; i<list.size(); i++){
				 BaseRealization br = list.get(i);
				 long minStartTime = getBotAvailableTime(br.getBot());
				 Route r1 = findRoutes1(br,minStartTime);
				 if( r1==null ){
					 //findRoutes1(br,minStartTime);
					 return false;
				 }
			}
			 
			for( int i=0; i<list.size(); i++){
				 BaseRealization br = list.get(i);
				 long minStartTime = getBotAvailableTime(br.getBot());
				 Route r2 = findRoutes2(br,minStartTime);
				 if( r2==null ){
					 //findRoutes2(br,minStartTime);
					 return false;
				 }
				 
				 Conflict c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);
				 if( c!=null ) {
					//TODO debug only
					 c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);  
					 if( !env.allowCollision() ){
						 throw new RealizationException("Routes conflict occured in the Schedule after creating new Realization after Blockers removing.");
					 }
				 }
			}


		}

		
		return true;
	}
	
	protected Route findRoutes1( BaseRealization br, long startTime ){
		
//		//int routeGroupId = Route.getNextRouteId();
//		 
//		//long startTime = br.getBotAvailable();
//		long minTimeAtMvc = br.getMinimalEndTime(); 
//		long maxStartTime = minTimeAtMvc; //TODO has to be change
//		
////		while(startTime >= maxStartTime)//TODO has to be change
////			maxStartTime += 100;
//		long delta = 4000;
//		
//		if( startTime+delta >= maxStartTime ){
//			maxStartTime = startTime+delta;
//		}
		
		long mvcAvailableTime = getLocSchedule().getMvcAvailableTime( br.getBaseRequest().getMvc() );
		long maxStartTime = Math.max( startTime+10*NEXT_SOLUTION_TIME_DELTA, mvcAvailableTime+3600*1000);
		
		
		for( ; startTime <= maxStartTime; startTime+=NEXT_SOLUTION_TIME_DELTA){
		
			 Route r1 = routeFirst( br, Environment.TIME_TO_PICK_UP_PRODUCT, startTime );
			 
			 if( r1==null ) {
				 continue;
			 }
			 if( !ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env) ){
				log.error("Wrong route 1");
			 }
			 
			 addRoute(r1,br);			 
			 
			 boolean blokersRemoved =  moveBlokersBots( br.getBot(), br ) ;
			 
			 if( blokersRemoved ){
				 return r1;//solved
			 }
			 //No solution yet

			 //moveBlokersBots( br.getBot(), br, routeGroupId ) ;
			 realization.removeScheduleItems( br.getItems() );
		}
		br.getBaseRequest().setTimeToReschedule(maxStartTime+1000);
		return null;
	}
	
	protected Route findRoutes2( BaseRealization br, long startTime ){
		
//		 int routeGroupId = Route.getNextRouteId();
//		 
////		long startTime = br.getBotAvailable();
//		long minTimeAtMvc = br.getMinimalEndTime(); 
//		long maxStartTime = minTimeAtMvc; //TODO has to be change
//		
////		while(startTime >= maxStartTime)//TODO has to be change
////			maxStartTime += 100;
//		long delta = 4000;
//		
//		if( startTime+delta >= maxStartTime ){
//			maxStartTime = startTime+delta;
//		}
		
		long mvcAvailableTime = getLocSchedule().getMvcAvailableTime( br.getBaseRequest().getMvc() );
		long maxStartTime = Math.max( startTime+10*NEXT_SOLUTION_TIME_DELTA, mvcAvailableTime+3600*1000);
		
		for( ; startTime <= maxStartTime; startTime+=NEXT_SOLUTION_TIME_DELTA){
			 
			 Route r2 = routeSecond( br, Environment.TIME_TO_STAY_IN_MVC, startTime );
			 if( r2==null ) {
				 continue;
			 }
			 if( !ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env) ){
				log.error("Wrong route 2");
			 }
			 
			 addRoute(r2,br);
			 
			 boolean blokersRemoved =  moveBlokersBots( br.getBot(), br ) ;
			 
			 if( blokersRemoved ){
				 return r2;//solved
			 }
			 //No solution yet

			 //moveBlokersBots( br.getBot(), br, routeGroupId ) ;
			 realization.removeScheduleItems( br.getItems() );
		}
		br.getBaseRequest().setTimeToReschedule(maxStartTime+1000);
		return null;
	}
	
	@Override
	protected final Route routeSecond( BaseRealization br, long timeToStay, long startTime ){
		
		//currentRouteItems = new ArrayList<ScheduleItem>();
		
		BaseRequest req = br.getBaseRequest();
		Pos p1 = getLastPos( br.getBot() );
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

	
	protected void addRoute( Route r1,  BaseRealization br ){

		 r1.setRouteGroupId(br.getRoutesGroupId());
		 
		 ScheduleItem si1 = new ScheduleItem(r1);
		 
		 addScheduleItem( si1 );
		 
		 br.addItem(si1, true);
	}


}
