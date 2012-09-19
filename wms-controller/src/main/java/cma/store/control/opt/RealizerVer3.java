package cma.store.control.opt;

import java.util.ArrayList;
import java.util.List;

import cma.store.config.SettingProperties;
import cma.store.control.BaseRealization;
import cma.store.control.Controller;
import cma.store.control.Realization;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.allshortestpaths.FloydWarshallAlgorithm;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Blocker;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;

public class RealizerVer3 extends RealizerVer1 {
	private static final int NEXT_SOLUTION_TIME_DELTA = 500;
	private static final int NEXT_SOLUTION_TIME_DELTA_REALISTIC = 2500;
	private static final int MAX_START_TIME_OFFSET = 3600*1000;
	private static final int MAX_START_TIME_OFFSET_REALISTIC = 80000;
	private static final int MAX_RECURSIVE_IT = 5;
	
	private List<BaseRealization> all;
	private int requestListId;
	private List<Bot> botsToChose;
	private int subListSize;
	
	private int nextSolDeltaTime;
	private int maxStartTimeOffset;


	public RealizerVer3(Environment env) {
		super(env);
		nextSolDeltaTime = NEXT_SOLUTION_TIME_DELTA;
		maxStartTimeOffset = MAX_START_TIME_OFFSET;
		boolean useRealisticTimes = Boolean.parseBoolean((String) SettingProperties
                .getInstance().getValue("external.properties",
                        Controller.REALISTIC_TIMES));
		if (useRealisticTimes) {
			nextSolDeltaTime = NEXT_SOLUTION_TIME_DELTA_REALISTIC;
			maxStartTimeOffset = MAX_START_TIME_OFFSET_REALISTIC;
		}
	}
	
	private void removeBaseRealization( BaseRealization br, Realization rel ){
		
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
	
	protected void checkConflicts(String place) throws RealizationException{
		 
		 Conflict c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);
		 if( c!=null ) {
			 log.debug("Conflicted routes (" + place +")- shouldn't happens ");
			 c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);  
			 if( !env.allowCollision()){
				 throw new RealizationException("Routes conflict occured in the Schedule after creating new Realization after Blockers removing.");
			 }
		 }
	}
	
	protected boolean haveConflicts(){
		 
		 Conflict c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);
		 if( c!=null ) {
			 log.debug("Conflicted routes (blockers) - shouldn't happens ");
			 c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);  
			 return true;
		 }
		 
		 return false;
	}
	
	
	@Override
	protected boolean findRealization(int it) throws RealizationException {
		
//		if( shortestPathsFinder==null ){
//			shortestPathsFinder = new FloydWarshallAlgorithm(env);
//		}
		
		
		createNewRealization();
		assignBots();
		//debug
		
		all = realization.getBaseRealList();
		requestListId=-1;
		subListSize = Math.min( Math.min( env.getBotFleet().getBots().size(),  all.size() ), env.getBotFleet().getBots().size() );
//		subListSize  = all.size();
		
		List<BaseRealization> list;
		int requestDone = 0;
		
		for( int j=0; j<all.size(); j+=requestDone){
			
			int to = Math.min(j+subListSize, all.size() );
			list = all.subList(j, to);
			if( list==null ) break;		
			
			botsToChose = new ArrayList<Bot>();
			botsToChose.addAll( env.getBotFleet().getBots() );
			
			//look for bes realizations;
			for( int i=0; i<list.size(); i++){
				 BaseRealization br = list.get(i);
				 updateProdAndBot(br,it);
			}
			
			requestDone=0;
			for( int i=list.size()-1; i>=0; i--){ //for tests only
				 BaseRealization br = list.get(i);
				 log.debug("processing request = " + br );
				 
				 if( br.getBot()==null){
					 break;
				 }
				 requestDone++;
				 long minStartTime = getBotAvailableTime(br.getBot());
				 Route r1 = findRoutes1(br,minStartTime);
				 if( r1==null ){
					 //findRoutes1(br,minStartTime);
					 return false;
				 }
				 
				 checkConflicts("Route_1");

			}
			 
			for( int i=0; i<requestDone; i++){
				 BaseRealization br = list.get(i);
				 long minStartTime = getBotAvailableTime(br.getBot());
				 Route r2 = findRoutes2(br,minStartTime);
				 if( r2==null ){
					 //findRoutes2(br,minStartTime);
					 return false;
				 }
				 
				 checkConflicts("Route_2");
				 
			}
		}

		
		return true;
	}
	
	protected Route findRoutes1( BaseRealization br, long startTime ) throws RealizationException{
		 
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
		long maxStartTime = Math.max( startTime+10*nextSolDeltaTime, mvcAvailableTime+maxStartTimeOffset);
		
		for( ; startTime <= maxStartTime; startTime+=nextSolDeltaTime){
		
			 Route r1 = routeFirst( br, Environment.TIME_TO_PICK_UP_PRODUCT, startTime );
			 
			 if( r1==null ) {
				 continue;
			 }
			 if( !ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env) ){
				log.error("Wrong route 1");
			 }
			 
			 ScheduleItem si = addRoute(r1,br,true);
			 
			 List<Blocker> newBlockers = new ArrayList<Blocker>();
			 addBlockers( r1.getBot(), r1.getBlockers(), newBlockers );
			 boolean blokersRemoved =  moveBlokersBots( newBlockers, 0, br ) ;
			 
			 if( blokersRemoved ){
				 if( haveConflicts() ) {
					 removeRoute( si, br, true );
				 }else{
					 return r1;//solved
				 }
			 }else{
				 removeRoute( si, br, true );
			 }
		}
		br.getBaseRequest().setTimeToReschedule(maxStartTime+1000);
		return null;
	}
	
	protected Route findRoutes2( BaseRealization br, long startTime )  throws RealizationException{
		
		//currentRouteItems = new ArrayList<ScheduleItem>();
		
		long mvcAvailableTime = getLocSchedule().getMvcAvailableTime( br.getBaseRequest().getMvc() );
		long maxStartTime = Math.max( startTime+10*nextSolDeltaTime, mvcAvailableTime+maxStartTimeOffset);
		
		for( ; startTime <= maxStartTime; startTime+=nextSolDeltaTime){
			//currentRouteItems = new ArrayList<ScheduleItem>();
			 
			//int countItems = getLocSchedule().getItems().size();
			
			 Route r2 = routeSecond( br, Environment.TIME_TO_STAY_IN_MVC, startTime );
			 if( r2==null ) {
				 continue;
			 }
			 
			 if( env.isDeepCheck() ){
				 if( !ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env) ){
					log.error("Wrong route 2");
				 }
			 }
			 
			 ScheduleItem si = addRoute(r2,br,true);
			 
			 List<Blocker> newBlockers = new ArrayList<Blocker>();
			 addBlockers( r2.getBot(), r2.getBlockers(), newBlockers );
			 boolean blokersRemoved =  moveBlokersBots( newBlockers, 0, br ) ;
			 
			 if( blokersRemoved ){
				 if( haveConflicts() ) {
					 removeRoute( si, br, true );
				 }else{
					 return r2;//solved
				 }
			 }else{
				 removeRoute( si, br, true );
			 }

		}
		br.getBaseRequest().setTimeToReschedule(maxStartTime+1000);
		return null;
	}
	
	protected Route routeSecond( BaseRealization br, long timeToStay, long startTime ){
		
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

	
	protected ScheduleItem addRoute( Route route,  BaseRealization br, boolean baseRoute ){
		
		
		 route.setRouteGroupId(br.getRoutesGroupId());
		 
		 ScheduleItem si1 = new ScheduleItem(route);
		 
		 //currentRouteItems.add( si1 );
		 
		 addScheduleItem( si1 );
		 
		 br.addItem(si1, baseRoute);
		 
		 if( !baseRoute ){
			 route.setType(RouteType.BLOCKER );
		 }
		 
		 return si1;
	}
	
	protected void removeRoute(  ScheduleItem si, BaseRealization br, boolean baseRoute ){
		
		realization.removeScheduleItem(si);
		
		br.removeItem( si, baseRoute );

	}	
	
	
	/**
	 * Return true when all blockers are removed
	 */
	protected boolean moveBlokersBots( List<Blocker> blockers, int recursiveId, BaseRealization br ){
		
		if( blockers==null || blockers.size()==0 ) return true; //nothing to do
		if( MAX_RECURSIVE_IT <= recursiveId ) return false;
		
		List<ScheduleItem> tmpRoutes = new ArrayList<ScheduleItem>();
		List<Blocker> newBlockers = new ArrayList<Blocker>();
		
//		int countItems = getLocSchedule().getItems().size();
		
		for( Blocker blocker: blockers ){
			
			Bot bot = blocker.getBlocker();
			Route r = getLastRoute( bot );
			
			Pos posBotEnd;
			long timeBotEnd;
			
			if( r==null ){
				posBotEnd = bot.getPos();
				timeBotEnd = 0;
			}else{
				posBotEnd = r.getFinalPos();
				timeBotEnd = r.getFinalTime();
			}
			
			Conflict conflict = ConflictFinder.getFirstConflictAfterTime( bot, posBotEnd, timeBotEnd, getLocSchedule(), env );
			if( conflict==null ) continue;  //already fixed
			long deltaTime = 5*env.getTimeUnitMs();
			
			boolean solutionFounded=false;
			
			for( long startTime=timeBotEnd; startTime<=conflict.time; startTime+=deltaTime){
						
				//try to move with new blockers
				Request request = new Request( RequestType.KNOWN_START_AND_TIME, startTime, posBotEnd, null, null, null, 0, br.getBaseRequest() );
				ConfigurableFreeRouteController c = new ConfigurableFreeRouteController(env,request,false,true,getLocSchedule());
				Route route  = getRouteCreator().createRout( request, bot, c, FORWARD_ROUTE );
				
				if( route ==null ){
					continue;
				}
				
				ScheduleItem si = addRoute(route,br,false);
				tmpRoutes.add( si );
				
				boolean anyBlockers = route.getBlockers()!=null && route.getBlockers().size()>0;
				
				if( anyBlockers ){
					solutionFounded=true;
					addBlockers( route.getBot(), route.getBlockers(), newBlockers );
					break;
				}else{
					solutionFounded=true;
					break;
				}
			}
			
			if( !solutionFounded ){
				for( ScheduleItem si: tmpRoutes ){
					removeRoute( si, br, false );
				}
				return false;
			}
			
			boolean blockersSolved = true;
			if( newBlockers!=null && newBlockers.size()>0 ){
				blockersSolved = moveBlokersBots( newBlockers, recursiveId++, br );
			}
		 
			
			if( !blockersSolved ){
				for( ScheduleItem si: tmpRoutes ){
					removeRoute( si, br, false );
				}
				return false;
			}
		}
		
		return true;

	}
	
	private List<Blocker> conflicts2blockers( Bot movedBot, List<Conflict> newConflicts ){

		if( newConflicts==null || newConflicts.size()==0 ) return null;
		List<Blocker> blockers = new ArrayList<Blocker>();
		for( Conflict ccc: newConflicts ){
			Bot b;
			if( ccc.b1==movedBot ){
				blockers.add( new Blocker(ccc, ccc.b2));
			}else if( ccc.b2==movedBot ){
				blockers.add( new Blocker(ccc, ccc.b1));
			}else{
				continue;
			}
		}
		
		
		return blockers;
	}
	
	private void addBlockers( Bot movedBot, List<Conflict> newConflicts, List<Blocker> blockers ){
		
		List<Blocker> add = conflicts2blockers( movedBot, newConflicts );
		if( add==null ) return;
		
		for(Blocker n: add ){
			boolean shouldAdd = true;
			for(int i=blockers.size()-1; i>=0; i--){
				Blocker o = blockers.get(i);
				if( o.getBlocker()==n.getBlocker() ){
					if( n.getConflict().time < o.getConflict().time ){
						blockers.remove( i );
						break;
					}else{
						shouldAdd = false;
						break;
					}					
				}
				
			}
			if( shouldAdd ){
				blockers.add( n );
			}
		}
		
	}
	

}
