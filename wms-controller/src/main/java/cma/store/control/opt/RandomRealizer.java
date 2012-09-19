package cma.store.control.opt;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

import cma.store.control.BaseRealization;
import cma.store.control.Realization;
import cma.store.control.RealizationScoreCalculator;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.RouteCreator;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.control.teamplaninng.TaskAssignService;
import cma.store.control.teamplaninng.TaskAssignServiceImpl;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteAbs;
import cma.store.data.RouteImp;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.env.Timer;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.Schedule;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Conflict;
import cma.store.utils.ConflictFinder;
//import cma.store.view.DisplaySimple;

public class RandomRealizer implements Realizer{
	Logger log = Logger.getLogger(getClass());
	
	protected static final boolean FORWARD_ROUTE = true;
	protected static final boolean BACK_ROUTE = false;
	
	private RealizationScoreCalculator scoreCalculator;
	private int minIt;
	private Realization best;
	protected Environment env;
	protected List<BaseRequest> baseRequestList;
	protected int it;
	protected Random rnd;
	protected Realization realization;
	TaskAssignService taskAssignService;
	
	public RandomRealizer( Environment env ){
		this.env = env;
		scoreCalculator = new RealizationScoreCalculator();
		taskAssignService = new TaskAssignServiceImpl();
		rnd = new Random( env.getSeed() );
		
		if( env.isDebugMode() ){
			switchOnDebugView();
		}
	}
	
	private void switchOnDebugView(){
		
		Timer timer = new Timer(){

			@Override
			public long getTimeMs() {
				return Long.MAX_VALUE;
			}
		};

//		DisplaySimple ds = new DisplaySimple(env, timer);
//		ds.start();
		
	}
	
	protected RouteCreator getRouteCreator(){
		return env.getRouteCreator();
	}
	
	protected Schedule getLocSchedule(){
		return realization.getLocalSchedule();
	}
	
	protected Route routeFirst( BaseRealization br, long timeToStay, long startTimeMin){
		
		BaseRequest req = br.getBaseRequest();
		List<LocPriority> locPriority = br.getProdLocPtiority();
		Pos p1 = br.getBotStartPosition();
		
		Request r = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, startTimeMin, p1, null, null, locPriority, timeToStay, req );

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());
		
		Route route =  getRouteCreator().createRout( r, br.getBot(), c1, FORWARD_ROUTE );
		if( route!=null ){
			route.setType(RouteType.ROUTE_1);
		}
		return route;
	}
	
	protected Route routeSecond( BaseRealization br, long timeToStay, long startTimeMin ){
		
		BaseRequest req = br.getBaseRequest();
		Pos p1 = getLastPos(br.getBot());
		Pos p2 = req.getMvc().getPos();
		
		Request r = new Request( RequestType.KNOWN_END_AND_BEFORE_END_TIME, startTimeMin, p1, null, p2, null, timeToStay, req );
		r.setBaseRequest(req);

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());
		Route route = getRouteCreator().createRout( r, br.getBot(), c1, FORWARD_ROUTE );
		
		if( route!=null ){
			route.setType(RouteType.ROUTE_2);
			br.setDestinationPriority( c1.getDestinationPriority() );
		}
		return route;
	}
	
	
	
	
	public Realization findRealization( List<BaseRequest> baseRequestList ) throws RealizationException {
		
		this.baseRequestList = baseRequestList;
		
		best=null;
		it=1; // previous version was it=-1
		minIt=3;
		
		log.debug("Processing new requests (count=" + baseRequestList.size() +")" );
	
		do{
			it++;
			
			Schedule s=null;
//			if( env.isDebugMode() ){
//				s = env.getSchedule();  //debug only
//			}
			
			boolean ok = findRealization( it );
			
//			if( env.isDebugMode() ){
//				env.setSchedule(s);  //debug only
//			}
			
			if( !ok ){
				continue;
			}
			
			scoreCalculator.updateScore( realization );
			
			dumpRequestsPlan();
			
			log.debug( "Realization score=" + realization.getScore() );
			
			if( best==null || best.getScore() < realization.getScore() ){
				best = realization;
			}
			
			if( stop()) {
				break;
			}
			
		}while( true );
		
		
		if( best!=null ){
			env.getSchedule().update( best );

			log.debug( "Best realization score=" + best.getScore() );
		}else{
			log.debug( "No solution founded" );
		}
		
		Conflict c = ConflictFinder.haveConflicts(env.getSchedule(), env);
		if( c!=null ) {
			log.fatal("Conflict occured in the Schedule after creating new Realization." + c );
		}
	
		return best;
	}
	
	private void dumpRequestsPlan() {
		for (BaseRealization real : realization.getBaseRealList()) {
			log.info("For Order "+real.getBaseRequest().getOrderId()+" bot choose:"+real.getBot());
		}
	}


	protected final void createNewRealization() {
		
		realization = Realization.createRealization(baseRequestList,env);
		
		for( BaseRealization br: realization.getBaseRealList() ){
//			
//			long mvcAvailable = env.getSchedule().getMvcAvailableTime( br.getBaseRequest().getMvc() );
//			if(br.getMinimalEndTime() == 0l) {
//				br.setMinimalEndTime(mvcAvailable);
//			}
//			mvcAvailable += env.TIME_TO_STAY_IN_MVC*2;
		}
		
		if( env.isDebugViewMode() ){ 
			env.setSchedule( realization.getLocalSchedule() );
		}
		
	}

	protected long getLastUsedTime( Mvc mvc, int location ){
		return env.getSchedule().getMvcUseEndTime(mvc, location);
	}

	private boolean stop(){
		return best!=null && it>=minIt-1;
	}
	
	protected Route getLastRoute( Bot bot ){
		return getLocSchedule().getLastRoute(bot);
	}
	
	protected long getBotAvailableTime( Bot bot ){
		return getLocSchedule().getAvailableMinTime(bot);
	}
	
	protected Pos getLastPos( Bot bot ){
		return getLocSchedule().getLastPos(bot);
	}
	
	protected Bot chooseBot( int it ){
		int size = env.getBotFleet().getBots().size();
		Bot bot  = env.getBotFleet().getBots().get( rnd.nextInt(size) ); //should be chose differently
		return bot;
	}
	
	
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
	
	protected void updateProdLocPriority( BaseRealization br ){
		
		List<LocPriority> list = new ArrayList<LocPriority>();
		list.addAll(br.getBaseRequest().getStorageLocations()); //could be choosed differently
		
		br.setProdLocPtiority( list );
	}
	
	/**
	 * Return true when all blockers are removed
	 */
	protected boolean moveBlokersBots( Bot movedBot, BaseRealization br ){
		int maxIt = 10;
		boolean anyBlockers = true;
		boolean anyUnresolved=false;
		boolean anyMove = false;
		
		for(int i=0; i<=maxIt; i++){
			anyBlockers = false;
			anyUnresolved = false;
			anyMove = false;
			
			for( Bot bot: env.getBotFleet().getBots() ){

				Pos posBotEnd;
				long timeBotEnd;
				Route r = getLastRoute( bot );
				
				if( r==null ){
					posBotEnd = bot.getPos();
					timeBotEnd = 0;
				}else{
					posBotEnd = r.getFinalPos();
					timeBotEnd = r.getFinalTime();
				}
				
				Conflict conflict = getFirstConflictAfterTime(bot, posBotEnd, timeBotEnd);
				if( conflict==null ) continue;
				
				anyBlockers = true;
	
				//ToDo for debug only
				getFirstConflictAfterTime(bot, posBotEnd, timeBotEnd);
				if( i>=maxIt){
					return false;
				}
				
				boolean solutionFouded = false;
				
				long timeStep = 5*env.getTimeUnitMs();
				
				for(long moveTime=timeBotEnd; moveTime<=conflict.time; moveTime+=timeStep ){
					//try to move withouth any new accidents
					Request request = new Request( RequestType.KNOWN_START_AND_TIME, moveTime, posBotEnd, null, null, null, 0, br.getBaseRequest() );
					ConfigurableFreeRouteController c = new ConfigurableFreeRouteController(env,request,false,true,getLocSchedule());
					Route route  = getRouteCreator().createRout( request, bot, c, FORWARD_ROUTE );
					if( route!=null){
						anyMove = true;
						route.setType(RouteType.BLOCKER);
						route.setRouteGroupId(br.getRoutesGroupId());
						ScheduleItem si =  new ScheduleItem(route);
						br.addItem(si, false);
						addScheduleItem( si );
						solutionFouded = true;
						break;
					}else{
						//route  = getRouteCreator().createRout( request, bot, c, FORWARD_ROUTE );
						continue;
					}
				}
				if( !solutionFouded ) {
					anyUnresolved=true;
				}
			}
//			if( !anyBlockers ){
//				break;
//			}
			if( !anyMove ){
				break;
			}
		}
		
		if( !anyUnresolved ){
		 Conflict c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);
		 if( c!=null ) {
			 //TODO debug only
			 c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);  
		 }
		}
		
		return !anyUnresolved;
	}
	
	protected void addScheduleItem( ScheduleItem si ){
		realization.addScheduleItem( si );
	}
	
	private Conflict getFirstConflictAfterTime( Bot bot, Pos pos, long time){
		
		return ConflictFinder.getFirstConflictAfterTime(bot, pos, time, getLocSchedule(), env );
		
	}
	
	protected boolean findRealization(int it) throws RealizationException {
		
		createNewRealization();
//		assignBots();
		
		int count = realization.getBaseRealList().size();
		
		for(int i=0; i<count;i++){
			int id = i;
			
			log.debug("Request nr = " + realization.getBaseRealList().get(id).getBaseRequest().getOrderId());
			
			if( !findMoveRealization(it, id) ){
				return false;
			}
		}
		
		return true;
	}
	
	
	protected void assignBots() {
		taskAssignService.assignBotToRequests3(realization);
	}


	protected boolean findRoutes( BaseRealization br ){

		 Route r1 = routeFirst( br, Environment.TIME_TO_PICK_UP_PRODUCT, br.getBotAvailable() );
		 
		 if( r1==null ) return false;
		 if( !ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env) ){
			log.error("Wrong route 1");
		 }
		 
		 long startTime = r1.getFinalTime();
		 Route r2 = routeSecond( br, Environment.TIME_TO_STAY_IN_MVC, startTime );
		 if( r2==null ) return false;
		 if( !ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env) ){
			log.error("Wrong route 2");
		 }
		 
		 addRoutes(r1,r2,br);
		 
		 boolean blokersRemoved =  moveBlokersBots( br.getBot(), br ) ;
		 
		 if( !blokersRemoved ){
//			 moveBlokersBots( br.getBot(), br, routeGroupId ) ;
			 realization.removeScheduleItems( br.getItems() );
			 return false;
		 }
		 
		 return true;
	}
	
	protected void addRoutes( Route r1, Route r2,  BaseRealization br ){

		 r1.setRouteGroupId(br.getRoutesGroupId());
		 r2.setRouteGroupId(br.getRoutesGroupId());
		 
		 ScheduleItem si1 = new ScheduleItem(r1);
		 ScheduleItem si2 = new ScheduleItem(r2);
		 
		 addScheduleItem( si1 );
		 addScheduleItem( si2 );
		 
		 br.addItem(si1, true);
		 br.addItem(si2, true);
	}
	
	protected final void updateProdAndBot( BaseRealization br, int it ){
		updateProdLocPriority(br);
		updateBot(br,it);		
		br.setRoutesGroupId( RouteAbs.getNextRouteId() );
	}

	
	protected boolean findMoveRealization( int it, int id ) throws RealizationException {
		
		 BaseRealization br = realization.getBaseRealList().get(id);
		 updateProdAndBot( br, it );
		 
		 if( !findRoutes(br) ){
			 return false;
		 }
		 
		 Conflict c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env);
		 if( c!=null ) {
			 
//			 moveBlokersBots( br.getBot(), br, routeGroupId );
			 
//			 br.getItems();
//			 realization.removeScheduleItems( br.getItems() );
//			 br.getItems().clear();
//			 findRoutes(br);//TODO for debuging only
			 
			 c = ConflictFinder.haveConflicts(realization.getLocalSchedule(), env); //TODO debug only
			 
//			 blokersRemoved =  moveBlokersBots( br.getBot(), routesToCheck, br, routeGroupId ) ;
			 
			 ConflictFinder.haveConflicts(realization.getLocalSchedule(), env); //TODO for debuging only
			 log.fatal("Conflict occured in the Schedule after creating new Realization after Blockers removing.");
			 
//			 blokersRemoved =  moveBlokersBots( br.getBot(), routesToCheck, br, routeGroupId ) ;
			 
			 if( env.allowCollision() ){
				 throw new RealizationException("Routes conflict occured in the Schedule after creating new Realization after Blockers removing.");
			 }
		 }
		 
		 return true;

	}

	@Override
	public String getAlgorithmName() {
		return getClass().getSimpleName();
	}
	

}
