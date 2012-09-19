/**
 * 
 */
package cma.store.control.opt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cma.store.control.BaseRealization;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.data.RequestType;
import cma.store.control.opt.route.simple.BackwardsRouteCreator;
import cma.store.control.opt.route.tools.ConfigurableFreeRouteController;
import cma.store.data.Bot;
import cma.store.data.Mvc;
import cma.store.data.Pos;
import cma.store.data.Route;
import cma.store.data.RouteType;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.BaseRequestCreator;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.ConflictFinder;
import cma.store.utils.Utils;

/**
 * @author Filip
 *
 */
public class BackwardsRealizer extends RealizerVer1 {
	private static final int NEXT_SOLUTION_TIME_DELTA = 500;
	BaseRequestCreator requestCreator;
	Map<Mvc, Long> lastRequestEndTime;

	public BackwardsRealizer(Environment env) {
		super(env);
		List<Mvc> mvcs = env.getMvcFleet().getInputMvcs();
		lastRequestEndTime = new HashMap<Mvc, Long>();
		for (Mvc mvc : mvcs) {
			lastRequestEndTime.put(mvc, (long) 0);
		}
	}
		
	protected boolean findRealization(int it) throws RealizationException {
		createNewRealization();
		
		int count = realization.getBaseRealList().size();
		
		for(int i=0; i<count;i++){
			int id = i;
			int currentOrderID = realization.getBaseRealList().get(id).getBaseRequest().getOrderId();
			
			log.debug("Request nr = " + currentOrderID);

			BaseRealization br = realization.getBaseRealList().get(id);
			Mvc mvc = br.getBaseRequest().getMvc();
			long mvcLastRequestEndTime = lastRequestEndTime.get(mvc);
			long minimalEndTime = mvcLastRequestEndTime + Environment.TIME_TO_STAY_IN_MVC; // TODO: delete time_to_stay
			br.setMinimalEndTime(minimalEndTime);
			
			if (!findMoveRealization(it, id)) {
				return false;
			}
			// Update endTime for the next request			
			lastRequestEndTime.put(mvc, br.getBaseRoutes().get(1).getFinalTime());
		}
		
		return true;
	}
	

	@Override
	protected boolean findRoutes( BaseRealization br ){
		
		long minStartTime = br.getBotAvailable(); // min start time
		Bot bot = br.getBot();
		Pos botStartPos = br.getBotStartPosition();
		long startTime = minStartTime;
		long minTimeAtMvc = br.getMinimalEndTime();
		minTimeAtMvc = Math.max(minTimeAtMvc, minStartTime
			// triangle inequality: we need sthg to estimate two way travel time, but don't know destination at the moment
			+ Utils.minTimeToTrevel(bot, botStartPos, br.getBaseRequest().getMvc().getPos())
			+ Environment.TIME_TO_PICK_UP_PRODUCT);
		long timeAtMvc = minTimeAtMvc - Environment.TIME_TO_STAY_IN_MVC; // time we want to get to MVC
		log.debug("timeAtMvc = " + timeAtMvc + " minStartTime = " + minStartTime);

		for( ; ; timeAtMvc += NEXT_SOLUTION_TIME_DELTA){ // TODO: whats the limit? next order minTimeAtMvc?
														 // maybe we need another matching bots to tasks?
			Route r2 = routeSecond(br, timeAtMvc);
			if (r2 == null) continue;
			if (minStartTime + Utils.minTimeToTrevel(bot, botStartPos, r2.getInitPosition())
					+ Environment.TIME_TO_PICK_UP_PRODUCT > r2.getStartTime())
				continue; // prune: impossible for bot to come to the storage when route_2 starts
			if (!ConflictFinder.verifyRoute(r2, realization.getLocalSchedule(), env)){
				log.error("Wrong route 2");
			}
			
			log.debug("## Route second found: " + r2);
			
			// loop with decreasing maxTimeInStorage
			long maxTimeInStorage = r2.getStartTime() - Environment.TIME_TO_PICK_UP_PRODUCT;
			maxTimeInStorage = Math.max(maxTimeInStorage, 0);
			Route r1 = routeFirst( br, maxTimeInStorage, r2.getInitPosition());
			 
			if (r1 == null) continue;
			if (minStartTime > r1.getStartTime())
				continue;
			if(!ConflictFinder.verifyRoute(r1, realization.getLocalSchedule(), env)){
				log.error("Wrong route 1");
			}
			
			log.debug("## Route first found: " + r1);

			addRoutes(r1, r2, br);
			
			boolean blokersRemoved =  moveBlokersBots(br.getBot(), br) ;
			 
			if (blokersRemoved){
				log.debug("## ORDER(" + br.getBaseRequest().getOrderId() + ") realized start: "
					+ r1.getStartTime() + " end: " + r2.getFinalTime());
				return true;	//solved
			}
			//No solution yet
			log.debug("## Routes rejected: r2: " + r2 + " r1: " + r1);
			realization.removeScheduleItems(br.getItems());
		}
	}
	
	protected void addRoute( Route r1, BaseRealization br, int routeGroupId ){
		 r1.setRouteGroupId(routeGroupId);
		 ScheduleItem si1 = new ScheduleItem(r1);
		 addScheduleItem( si1 );
		 br.addItem(si1, true);
	}
	
	private Route routeFirst( BaseRealization br, long maxTimeInStorage, Pos storagePos ){
		
		BaseRequest req = br.getBaseRequest();
		Pos p1 = getLastPos(br.getBot()); // TODO: check if this is really a bot's position
		
		Request r = new Request( RequestType.KNOWN_END_AND_AFTER_END_TIME_BACKWARDS, null, p1, maxTimeInStorage,
			storagePos, null, Environment.TIME_TO_PICK_UP_PRODUCT, req );

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());

		Route route = ((BackwardsRouteCreator) getRouteCreator()).createRouteBackwards( r, br.getBot(), c1 );
		if( route!=null ) {
			route.setType(RouteType.ROUTE_1);
			return route;
		}
		
		return null;
	}
	
	private Route routeSecond( BaseRealization br, long timeAtMvc ){
		
		BaseRequest req = br.getBaseRequest();
		Pos mvc = req.getMvc().getPos();
		List<LocPriority> locPriority = br.getProdLocPtiority();

		Request r = new Request( RequestType.KNOWN_END_AND_AFTER_END_TIME_BACKWARDS, null, null, timeAtMvc,
			mvc, locPriority, Environment.TIME_TO_STAY_IN_MVC, req );
		r.setBaseRequest(req);

		ConfigurableFreeRouteController c1 = new ConfigurableFreeRouteController(env,r,false,true,getLocSchedule());
		Route route = ((BackwardsRouteCreator) getRouteCreator()).createRouteBackwards( r, br.getBot(), c1 );
		
		if( route!=null ){
			br.setDestinationPriority( c1.getDestinationPriority() );
			route.setType(RouteType.ROUTE_2);
			return route;
		}
		return null;
	}

}
