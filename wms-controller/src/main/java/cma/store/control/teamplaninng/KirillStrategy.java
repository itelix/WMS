package cma.store.control.teamplaninng;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cma.store.control.mvc.IMVCController;
import cma.store.control.opt.BotProdLocation;
import cma.store.control.opt.RequestRealization;
import cma.store.control.opt.data.Request;
import cma.store.control.opt.route.KirillRouteCreator;
import cma.store.control.opt.route.allshortestpaths.ShortestPathsFinder;
import cma.store.control.utils.PathsContainer;
import cma.store.data.Bot;
import cma.store.data.Route;
import cma.store.data.RouteAbs;
import cma.store.data.RouteImp;
import cma.store.env.BaseEnvironment;
import cma.store.env.Environment;
import cma.store.input.request.BaseRequest;
import cma.store.input.request.LocPriority;
import cma.store.schedule.ScheduleItem;
import cma.store.utils.Utils;
import cms.store.utils.PositionUtils;

public class KirillStrategy implements TeamPlaninngStrategy {
	Logger log = Logger.getLogger(getClass());
	ShortestPathsFinder pathsFinder = null;
	double speed = BaseEnvironment.DEFAULT_MAX_BOT_SPEED_MM_PER_MS;
	double deltaTime;
	double deltaDist;
	
	Environment env;
	IMVCController mvcController;
	private KirillRouteCreator routeCreator;
	
	PathsContainer foundRoutes;
	
	public KirillStrategy(Environment env) {
		this.env = env;
		
		deltaDist = env.getLayerModel().getUnitSize();
		deltaTime = deltaDist/speed; //ms
		
		routeCreator = new KirillRouteCreator(env);
		foundRoutes = new PathsContainer(env);
	}
	
	
	@Override
	public BotProdLocation getCarProdLocation(List<Bot> bots,
		List<LocPriority> prodLocations, boolean[][] used) {
		
		double bestDist = Double.MAX_VALUE;
		int bestI=-1;
		int bestJ=-1;
		
		for(int i=0;i<bots.size();i++){
			if(!bots.get(i).isWork()) {
				for(int j=0;j<prodLocations.size();j++){
					if( prodLocations.get(j).isAssigned() ) continue;
					double d = Utils.distance( prodLocations.get(j), bots.get(i));
					if( d<bestDist ){
						bestDist = d;
						bestI = i;
						bestJ = j;
					}
				}
			}
		}
		
		if( bestI == -1 ) return null;
		bots.get(bestI).setWork(true);
		prodLocations.get(bestJ).setAssigned(true);
		return new BotProdLocation( bots.get(bestI), prodLocations.get(bestJ).getPos(), null );
	}
	
	public void recoverAfterFailedRouteSearch(BotProdLocation cp,
			BaseRequest request) {
		cp.getBot().setWork(false);
		request.getStorageLocations().get(0).setAssigned(false);
	}
	
	@Override
	public void assingRequestToBots(List<BaseRequest> requests,
			List<Bot> availableCars) {
		log.debug("assingRequestToBots");
		List<PlannedRequest> plannedRequests = new LinkedList<PlannedRequest>();
		for (BaseRequest request : requests) {
//			BotProdLocation cp = new BotProdLocation(availableCars, request.getStorageLocations(), null); //request.getCarProdLocation();
			BotProdLocation cp =this.getCarProdLocation(availableCars, request.getStorageLocations(), null);
//			BotProdLocation cp = null;
			int routeGroupId = RouteAbs.getNextRouteId();
			if (cp == null) {
//				throw new NoFreeBotException(request);
				break;
			}
			log.debug("Order: " + request.getOrderId() + " to bot: " + cp.getBot().getId());
			//Pos pos = cp.getPos(); //TODO it has to be change car pos is time dependent
			long startTime = env.getTimeMs()+1000;
			long timeToReachProduct = startTime + 
					PositionUtils.getTimeToTrevel(cp.getBot().getPos(), cp.getProdPos(), cp.getBot());

			Request r1 = new Request(null,startTime, cp.getBot().getPos(), timeToReachProduct, cp.getProdPos(),0,null);
			
//			List<Pos> route1Pos = routeCreator.getBestPathForOrder(r1, cp.getCar(), foundRoutes);
			Route route1 = routeCreator.createRoutWithDelays(r1, cp.getBot(), foundRoutes);
//			Route route1 = routeCreator.applyDurationToPosList(route1Pos);
			
			if (route1 == null) {
				// recover and process the next request
				recoverAfterFailedRouteSearch(cp, request);
				continue;
			}

			long timeAfterPickUp = route1.getFinalTime() + Environment.TIME_TO_PICK_UP_PRODUCT;

			Request r2 = new Request(null, timeAfterPickUp, r1.getTo(), null, request.getDestination(),0,null);
			
//			List<Pos> route2Pos = routeCreator.getBestPathForOrder(r2, cp.getCar(), foundRoutes);
			Route route2  =  routeCreator.createRoutWithDelays(r2, cp.getBot(), foundRoutes);
			if (route2 == null) {
				recoverAfterFailedRouteSearch(cp, request);
				continue;
			}
			
			route1.setRouteGroupId(routeGroupId);
			route2.setRouteGroupId(routeGroupId);
			
			env.getSchedule().add( new ScheduleItem(route1) );
			env.getSchedule().add( new ScheduleItem(route2) );
			
			PlannedRequest plannedRequest = new PlannedRequest();
			plannedRequest.setBot(cp.getBot());
//			plannedRequest.getPosList().addAll(route1.getPos());
//			plannedRequest.getPosList().addAll(route2.getPos());
			plannedRequest.getRoutes().add(route1);
			plannedRequest.getRoutes().add(route2);
			plannedRequest.setBaseRequest(request);
			plannedRequests.add(plannedRequest);
/*
 * XXX this code will be in other block - add this to
			List<Route> routes = new ArrayList<Route>();
		
			int routeGroupId = Route.getNextRouteId();
		
			route1.setRouteGroupId(routeGroupId);
			route2.setRouteGroupId(routeGroupId);
		
			routes.add(route1);
			routes.add(route2);
		
			request.setRequestRealization(new RequestRealization(routes));
*/		
		}
		
		// check if planed route is colliding with standing bot
		routeCreator.checkAndResolveColisionWithStandingBot(plannedRequests,foundRoutes);		// add route to Scheduler - all routes are OK

		
		for (PlannedRequest plannedRequest : plannedRequests) {
			BaseRequest request = plannedRequest.getBaseRequest();
			request.setRequestRealization(new RequestRealization(plannedRequest.getRoutes()));
			plannedRequest.toString();
		}
		
	}

	@Override
	public void setMVCController(IMVCController mvcController) {
		// TODO Auto-generated method stub
	}

}
